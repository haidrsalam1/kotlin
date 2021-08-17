/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import com.intellij.testFramework.TestDataFile
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(NativeBlackBoxTestSupport::class)
abstract class AbstractNativeBlackBoxTest {
    internal lateinit var blackBoxTestProvider: NativeBlackBoxTestProvider

    fun runTest(@TestDataFile testFilePath: String) {
        blackBoxTestProvider.getTestByTestDataFile(File(testFilePath).canonicalFile).runAndVerify()
    }
}
