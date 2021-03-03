/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.inspections;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.jetbrains.kotlin.test.TestRoot;
import org.junit.runner.RunWith;

/*
 * This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}.
 * DO NOT MODIFY MANUALLY.
 */
@SuppressWarnings("all")
@TestRoot("fir")
@TestDataPath("$CONTENT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class HLLocalInspectionTestGenerated extends AbstractHLLocalInspectionTest {
    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("../idea/testData/inspectionsLocal/redundantVisibilityModifier")
    public static class RedundantVisibilityModifier extends AbstractHLLocalInspectionTest {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
        }

        @TestMetadata("internalInPrivateClass.kt")
        public void testInternalInPrivateClass() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/internalInPrivateClass.kt");
        }

        @TestMetadata("overridePropertySetter.kt")
        public void testOverridePropertySetter() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/overridePropertySetter.kt");
        }

        @TestMetadata("publicOverrideProtectedSetter.kt")
        public void testPublicOverrideProtectedSetter() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/publicOverrideProtectedSetter.kt");
        }

        @TestMetadata("publicOverrideProtectedSetter2.kt")
        public void testPublicOverrideProtectedSetter2() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/publicOverrideProtectedSetter2.kt");
        }

        @TestMetadata("publicOverrideProtectedSetter3.kt")
        public void testPublicOverrideProtectedSetter3() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/publicOverrideProtectedSetter3.kt");
        }

        @TestMetadata("publicOverrideProtectedSetter4.kt")
        public void testPublicOverrideProtectedSetter4() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/publicOverrideProtectedSetter4.kt");
        }

        @TestMetadata("publicOverrideProtectedSetter5.kt")
        public void testPublicOverrideProtectedSetter5() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/publicOverrideProtectedSetter5.kt");
        }

        @TestMetadata("publicOverrideProtectedSetter6.kt")
        public void testPublicOverrideProtectedSetter6() throws Exception {
            runTest("../idea/testData/inspectionsLocal/redundantVisibilityModifier/publicOverrideProtectedSetter6.kt");
        }
    }

    @RunWith(JUnit3RunnerWithInners.class)
    @TestMetadata("testData/inspectionsLocal")
    public abstract static class InspectionsLocal extends AbstractHLLocalInspectionTest {
        @RunWith(JUnit3RunnerWithInners.class)
        @TestMetadata("testData/inspectionsLocal/redundantVisibilityModifierFir")
        public static class RedundantVisibilityModifierFir extends AbstractHLLocalInspectionTest {
            private void runTest(String testDataFilePath) throws Exception {
                KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
            }

            @TestMetadata("publicFunInPublicClass.kt")
            public void testPublicFunInPublicClass() throws Exception {
                runTest("testData/inspectionsLocal/redundantVisibilityModifierFir/publicFunInPublicClass.kt");
            }

            @TestMetadata("publicValInPublicClass.kt")
            public void testPublicValInPublicClass() throws Exception {
                runTest("testData/inspectionsLocal/redundantVisibilityModifierFir/publicValInPublicClass.kt");
            }
        }
    }
}
