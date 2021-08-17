/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.test

import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.test.services.JUnit5Assertions.fail
import org.jetbrains.kotlin.test.util.KtTestUtil.getHomeDirectory
import org.jetbrains.kotlin.test.util.KtTestUtil.getTestsRoot
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File
import java.net.URLClassLoader

class NativeBlackBoxTestSupport : BeforeTestExecutionCallback {
    override fun beforeTestExecution(extensionContext: ExtensionContext) = with(extensionContext) {
        junitTestInstance.blackBoxTestProvider = getOrCreateBlackBoxTestProvider()
    }

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(NativeBlackBoxTestSupport::class.java.simpleName)

        /** Creates a single instance of [NativeBlackBoxTestProvider] per test class. */
        private fun ExtensionContext.getOrCreateBlackBoxTestProvider(): NativeBlackBoxTestProvider =
            root.getStore(NAMESPACE).getOrComputeIfAbsent(requiredTestClass) {
                createBlackBoxTestProvider(
                    NativeBlackBoxTestEnvironment(
                        testsRoot = testsRoot,
                        target = target,
                        testSourcesDir = testSourcesDir.apply {
                            deleteRecursively() // Clean-up the directory with all potentially stale generated sources.
                        },
                        testBinariesDir = testBinariesDir.apply {
                            deleteRecursively() // Clean-up the directory with all potentially stale executables.
                        },
                        kotlinNativeHome = kotlinNativeHome,
                        kotlinNativeClassLoader = kotlinNativeClassLoader
                    )
                )
            }.cast()

        private val ExtensionContext.junitTestInstance: AbstractNativeBlackBoxTest
            get() = requiredTestInstance.cast()

        private val ExtensionContext.testsRoot: File
            get() = File(getHomeDirectory()).resolve(getTestsRoot(requiredTestClass)).canonicalFile

        private val kotlinNativeHome: File
            get() = System.getProperty(KOTLIN_NATIVE_HOME)?.let(::File) ?: fail { "Non-specified $KOTLIN_NATIVE_HOME system property" }

        private val ExtensionContext.testSourcesDir: File
            get() = projectBuildDir.resolve("test-sources").resolve(requiredTestClass.sanitizedName)

        private val ExtensionContext.testBinariesDir: File
            get() = projectBuildDir.resolve("test-binaries").resolve(target.name).resolve(requiredTestClass.sanitizedName)

        private val projectBuildDir: File
            get() = System.getenv(PROJECT_BUILD_DIR)?.let(::File) ?: fail { "Non-specified $PROJECT_BUILD_DIR environment variable" }

        private val target: KonanTarget
            get() = HostManager.host

        private val Class<*>.sanitizedName: String
            get() = name.map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")

        // Use isolated cached class loader.
        private val kotlinNativeClassLoader: ClassLoader by lazy {
            val nativeClassPath = System.getProperty(KOTLIN_NATIVE_CLASSPATH)
                ?.split(':', ';')
                ?.map { File(it).toURI().toURL() }
                ?.toTypedArray()
                ?: fail { "Non-specified $KOTLIN_NATIVE_CLASSPATH system property" }

            URLClassLoader(nativeClassPath, /* no parent class loader */ null).apply { setDefaultAssertionStatus(true) }
        }

        private const val KOTLIN_NATIVE_HOME = "kotlin.native.home"
        private const val KOTLIN_NATIVE_CLASSPATH = "kotlin.internal.native.classpath"
        private const val PROJECT_BUILD_DIR = "PROJECT_BUILD_DIR"
    }
}
