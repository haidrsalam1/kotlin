/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.tests

import org.jetbrains.kotlin.konan.test.AbstractNativeBlackBoxTest
import org.jetbrains.kotlin.test.generators.generateTestGroupSuiteWithJUnit5

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "true")

    generateTestGroupSuiteWithJUnit5(args) {
        testGroup("native/tests-blackbox/tests-gen", "native/tests-blackbox/testData") {
            testClass<AbstractNativeBlackBoxTest> {
                model("blackbox", singleClass = true)
            }
        }
    }
}
