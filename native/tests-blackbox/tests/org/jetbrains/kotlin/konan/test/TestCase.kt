/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import java.io.File

internal typealias FreeCompilerArgs = Set<String>
internal typealias PackageName = String

internal data class TestFile(
    val name: String,
    val location: File,
    val contents: String
)

internal sealed interface TestCase {
    val files: List<TestFile>
    val freeCompilerArgs: FreeCompilerArgs

    sealed class Simple(
        override val files: List<TestFile>,
        override val freeCompilerArgs: FreeCompilerArgs,
        val testDataFile: File // The origin of the test case.
    ) : TestCase

    class Standalone(
        files: List<TestFile>,
        freeCompilerArgs: FreeCompilerArgs,
        testDataFile: File
    ) : Simple(files, freeCompilerArgs, testDataFile)

    class Regular(
        files: List<TestFile>,
        freeCompilerArgs: FreeCompilerArgs,
        testDataFile: File,
        val packageName: PackageName
    ) : Simple(files, freeCompilerArgs, testDataFile)

    class Composite(regularTestCases: List<Regular>) : TestCase {
        override val files: List<TestFile> = regularTestCases.flatMap { it.files }
        override val freeCompilerArgs: FreeCompilerArgs =
            regularTestCases.firstOrNull()?.freeCompilerArgs ?: emptySet() // Assume all compiler args are the same.
        val testDataFileToPackageNameMapping: Map<File, PackageName> = regularTestCases.associate { it.testDataFile to it.packageName }
    }
}
