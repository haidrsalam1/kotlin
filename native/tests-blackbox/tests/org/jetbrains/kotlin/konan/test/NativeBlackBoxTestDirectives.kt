/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object NativeBlackBoxTestDirectives : SimpleDirectivesContainer() {
    val STANDALONE by directive(
        description = "Compile this test as a standalone binary (don't include it into the shared test binary)"
    )

    val FILE by stringDirective(
        """
            Usage: // FILE: name.kt
            Declares file with specified name in current module
        """.trimIndent()
    )


    val FREE_COMPILER_ARGS by stringDirective(
        description = "Specify free compiler arguments for Kotlin/Native compiler"
    )
}
