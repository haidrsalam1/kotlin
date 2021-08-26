/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api.fir.types

import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.idea.frontend.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.idea.frontend.api.fir.utils.weakRef
import org.jetbrains.kotlin.idea.frontend.api.tokens.ValidityToken
import org.jetbrains.kotlin.idea.frontend.api.types.KtSubstitutor
import org.jetbrains.kotlin.idea.frontend.api.types.KtType
import org.jetbrains.kotlin.idea.frontend.api.withValidityAssertion

internal class KtFirSubstitutor(
    private val _substitutor: ConeSubstitutor,
    builder: KtSymbolByFirBuilder,
    override val token: ValidityToken,
) : KtSubstitutor {
    private val builderRef by weakRef(builder)
    val substitutor: ConeSubstitutor get() = withValidityAssertion { _substitutor }

    override fun substituteOrNull(type: KtType): KtType? = withValidityAssertion {
        require(type is KtFirType)
        substitutor.substituteOrNull(type.coneType)?.type?.let { builderRef.typeBuilder.buildKtType(it) }
    }
}