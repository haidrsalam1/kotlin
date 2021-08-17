/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import org.jetbrains.kotlin.test.services.JUnit5Assertions
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertEquals
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertNotNull
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail
import org.jetbrains.kotlin.test.services.impl.RegisteredDirectivesParser
import java.io.File

internal class NativeBlackBoxTestProvider(
    private val testDataFileToTestCaseMapping: Map<File, CompiledTestCase>
) {
    fun getTestByTestDataFile(testDataFile: File): NativeBlackBoxTest {
        val compiledTestCase = testDataFileToTestCaseMapping[testDataFile] ?: fail { "No test binary for test file $testDataFile" }

        val binary = compiledTestCase.binary // <-- Compilation happens here.
        val runParameters = when (val testCase = compiledTestCase.testCase) {
            is TestCase.Standalone -> NativeBlackBoxTestRunParameters.Empty
            is TestCase.Regular -> NativeBlackBoxTestRunParameters.WithPackageName(testCase.packageName)
            is TestCase.Composite -> NativeBlackBoxTestRunParameters.WithPackageName(
                packageName = testCase.testDataFileToPackageNameMapping.getValue(testDataFile)
            )
        }

        return NativeBlackBoxTest(binary, runParameters)
    }
}

internal fun createBlackBoxTestProvider(environment: NativeBlackBoxTestEnvironment): NativeBlackBoxTestProvider {
    val testDataFileToTestCaseMapping: MutableMap<File, CompiledTestCase> = mutableMapOf()
    val groupedRegularTestCases: MutableMap<FreeCompilerArgs, MutableList<TestCase.Regular>> = mutableMapOf()

    environment.testsRoot.walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .map { testDataFile -> createSimpleTestCase(testDataFile, environment) }
        .forEach { testCase ->
            when (testCase) {
                is TestCase.Standalone -> {
                    // Add standalone test cases immediately to the mapping.
                    testDataFileToTestCaseMapping[testCase.testDataFile] = testCase.toCompiledTestCase(environment)
                }
                is TestCase.Regular -> {
                    // Group regular test cases by compiler arguments.
                    groupedRegularTestCases.getOrPut(testCase.freeCompilerArgs) { mutableListOf() } += testCase
                }
            }
        }

    // Convert regular test cases into composite test cases and add the latter ones to the mapping.
    groupedRegularTestCases.values.forEach { regularCases ->
        val compositeTestCase = TestCase.Composite(regularCases).toCompiledTestCase(environment)
        regularCases.forEach { regularCase ->
            testDataFileToTestCaseMapping[regularCase.testDataFile] = compositeTestCase
        }
    }

    return NativeBlackBoxTestProvider(testDataFileToTestCaseMapping)
}

private fun createSimpleTestCase(testDataFile: File, environment: NativeBlackBoxTestEnvironment): TestCase.Simple {
    val relativeTestDir = testDataFile.parentFile.resolve(testDataFile.nameWithoutExtension).relativeTo(environment.testsRoot)
    val effectivePackageName = relativeTestDir.toPath().joinToString(".")

    val generatedSourcesDir = environment.testSourcesDir.resolve(relativeTestDir)

    val testFiles = mutableListOf<TestFile>()

    var currentTestFileName: String? = null
    val currentTestFileContents = StringBuilder()

    val directivesParser = RegisteredDirectivesParser(NativeBlackBoxTestDirectives, JUnit5Assertions)

    fun finishTestFile(newFileName: String?, lineNumber: Int) {
        if (currentTestFileName != null || currentTestFileContents.isNotBlank()) {
            val fileName = currentTestFileName ?: "main.kt"
            testFiles += TestFile(
                name = fileName,
                location = generatedSourcesDir.resolve(fileName),
                contents = currentTestFileContents.toString()
            )
        }

        currentTestFileContents.clear()

        if (newFileName != null) {
            currentTestFileName = newFileName
            repeat(lineNumber) { currentTestFileContents.appendLine() } // Preserve line numbers as in the original test data file.
        }
    }

    testDataFile.readLines().forEachIndexed { lineNumber, line ->
        val rawDirective = RegisteredDirectivesParser.parseDirective(line)
        if (rawDirective != null) {
            val parsedDirective = directivesParser.convertToRegisteredDirective(rawDirective)
            if (parsedDirective != null) {
                when (parsedDirective.directive) {
                    NativeBlackBoxTestDirectives.FILE -> {
                        val newFileName = getNewFileName(parsedDirective, testDataFile, lineNumber)
                        finishTestFile(newFileName, lineNumber)
                    }
                    else -> directivesParser.addParsedDirective(parsedDirective)
                }
                currentTestFileContents.appendLine()
                return@forEachIndexed
            }
        }

        currentTestFileContents.appendLine(line)
    }

    finishTestFile(newFileName = null, lineNumber = 0)

    val registeredDirectives = directivesParser.build()
    val freeCompilerArgs = registeredDirectives[NativeBlackBoxTestDirectives.FREE_COMPILER_ARGS].toSet()

    return if (NativeBlackBoxTestDirectives.STANDALONE in registeredDirectives)
        TestCase.Standalone(testFiles, freeCompilerArgs, testDataFile)
    else
        TestCase.Regular(
            files = testFiles.map { testFile -> fixPackageDeclaration(testFile, effectivePackageName, testDataFile) },
            freeCompilerArgs = freeCompilerArgs,
            testDataFile = testDataFile,
            packageName = effectivePackageName
        )
}

private fun getNewFileName(parsedDirective: RegisteredDirectivesParser.ParsedDirective, testDataFile: File, lineNumber: Int): String {
    val fileName = parsedDirective.values.singleOrNull()?.toString()
    assertNotNull(fileName) {
        """
            $testDataFile:$lineNumber: Exactly one file name expected in ${parsedDirective.directive} directive: ${parsedDirective.values}
            ${parsedDirective.directive.description}
        """.trimIndent()
    }
    assertTrue(fileName!!.endsWith(".kt") && fileName.length > 3 && '/' !in fileName && '\\' !in fileName) {
        "$testDataFile:$lineNumber: Invalid file name in ${parsedDirective.directive} directive: $fileName"
    }
    return fileName
}

private fun fixPackageDeclaration(testFile: TestFile, packageName: PackageName, testDataFile: File): TestFile {
    var existingPackageDeclarationLine: String? = null
    var existingPackageDeclarationLineNumber: Int? = null

    var inMultilineComment = false

    val lines = testFile.contents.lines()
    for ((lineNumber, line) in lines.withIndex()) {
        val trimmedLine = line.trim()
        when {
            inMultilineComment -> inMultilineComment = !trimmedLine.endsWith("*/")
            trimmedLine.isBlank() -> Unit
            trimmedLine.startsWith("/*") -> inMultilineComment = true
            else -> {
                // First meaningful line.
                if (trimmedLine.startsWith("package ")) {
                    existingPackageDeclarationLine = trimmedLine
                    existingPackageDeclarationLineNumber = lineNumber
                }
                break
            }
        }
    }

    return if (existingPackageDeclarationLine != null) {
        val existingPackageName = existingPackageDeclarationLine.substringAfter("package ").trimStart()
        assertEquals(packageName, existingPackageName) {
            "$testDataFile:$existingPackageDeclarationLineNumber: Invalid package name declaration found: $existingPackageDeclarationLine\nExpected: $packageName"
        }
        testFile
    } else
        testFile.copy(contents = "package $packageName ${testFile.contents}")
}
