/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api.components

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.idea.frontend.api.types.KtType
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode

public abstract class KtPsiTypeProvider : KtAnalysisSessionComponent() {
    public abstract fun asPsiType(
        type: KtType, context: PsiElement,
        mode: TypeMappingMode,
        defaultErrorType: PsiType?,
    ): PsiType
}

public interface KtPsiTypeProviderMixIn : KtAnalysisSessionMixIn {
    /**
     * Converts the given [KtType] to [PsiType].
     *
     * One can pass [TypeMappingMode] and default error type. For example, UAST wants the conversion
     * to be done in [TypeMappingMode.DEFAULT_UAST] mode and to return `UastErrorType` (of [PsiType]) if not successful.
     */
    public fun KtType.asPsiType(
        context: PsiElement,
        mode: TypeMappingMode = TypeMappingMode.DEFAULT,
        defaultErrorType: PsiType? = null
    ): PsiType =
        analysisSession.psiTypeProvider.asPsiType(this, context, mode, defaultErrorType)
}
