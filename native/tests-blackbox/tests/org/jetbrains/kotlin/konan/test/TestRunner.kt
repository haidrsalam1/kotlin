/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import org.jetbrains.kotlin.jvm.compiler.AbstractWriteSignatureTest.Companion.matchExact
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertEquals
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail

internal fun NativeBlackBoxTest.runAndVerify() {
    val args = listOfNotNull(
        binary.executableFile.path,
        if (runParameters is NativeBlackBoxTestRunParameters.WithPackageName) "--ktest_filter=${runParameters.packageName}*" else null,
        "--ktest_logger=GTEST"
    )

    val process = ProcessBuilder(args).directory(binary.executableFile.parentFile).start()

    TestOutput(runParameters, args, process).verify()
}

private class TestOutput(
    private val runParameters: NativeBlackBoxTestRunParameters,
    private val args: List<String>,
    private val process: Process
) {
    fun verify() {
        val exitCode = process.waitFor()
        val stdOut = process.inputStream.reader(Charsets.UTF_8).readText().trim()
        val stdErr = process.errorStream.reader(Charsets.UTF_8).readText().trim()

        fun details() = buildString {
            appendLine("\n\nRun arguments: $args")
            appendLine("Exit code: $exitCode")
            appendLine("\n== BEGIN [STDOUT] ==")
            if (stdOut.isNotEmpty()) appendLine(stdOut)
            appendLine("== END [STDOUT] ==")
            appendLine("\n== BEGIN [STDERR] ==")
            if (stdErr.isNotEmpty()) appendLine(stdErr)
            appendLine("== END [STDERR] ==")
        }

        assertEquals(0, exitCode) { "Process exited with non-zero code.${details()}" }
        assertTrue(stdErr.isEmpty()) { "Non-empty error output.${details()}" }

        val testStatuses = mutableMapOf<TestStatus, MutableSet<TestName>>()

        var expectStatusLine = false
        stdOut.lines().forEach { line ->
            when {
                expectStatusLine -> {
                    STATUS_LINE_REGEX.matchExact(line)?.let { matcher ->
                        val testStatus = matcher.group(1)
                        val testName = matcher.group(2)
                        testStatuses.getOrPut(testStatus) { mutableSetOf() } += testName
                    } ?: fail { "Malformed test output.${details()}" }
                    expectStatusLine = false
                }
                line.startsWith(RUN_LINE_PREFIX) -> expectStatusLine = true
                else -> Unit
            }
        }

        assertTrue(testStatuses.isNotEmpty()) { "No tests have been executed.${details()}" }

        val passedTests = testStatuses[STATUS_OK]?.size ?: 0
        assertTrue(passedTests > 0) { "No passed tests.${details()}" }

        if (runParameters is NativeBlackBoxTestRunParameters.WithPackageName) {
            val excessiveTests = testStatuses.getValue(STATUS_OK).filter { testName -> !testName.startsWith(runParameters.packageName) }
            assertTrue(excessiveTests.isEmpty()) { "Excessive tests have been executed: $excessiveTests.${details()}" }
        }

        val failedTests = (testStatuses - STATUS_OK).values.sumOf { it.size }
        assertEquals(0, failedTests) { "There are failed tests.${details()}" }
    }

    companion object {
        private const val RUN_LINE_PREFIX = "[ RUN      ]"
        private val STATUS_LINE_REGEX = Regex("^\\[\\s+([A-Z]+)\\s+]\\s+(\\S+)\\s+.*")
        private const val STATUS_OK = "OK"
    }
}

private typealias TestStatus = String
private typealias TestName = String
