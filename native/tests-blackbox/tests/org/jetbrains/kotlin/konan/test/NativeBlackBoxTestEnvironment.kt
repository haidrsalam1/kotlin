/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

internal class NativeBlackBoxTestEnvironment(
    val testsRoot: File, // The directory with original sources (aka testData).
    val target: KonanTarget,
    val testSourcesDir: File, // The directory with generated (preprocessed) test sources.
    val testBinariesDir: File, // The directory with generated test binaries (executable files).
    val kotlinNativeHome: File,
    val kotlinNativeClassLoader: ClassLoader
)
