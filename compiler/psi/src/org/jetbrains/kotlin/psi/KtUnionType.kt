/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.KotlinPlaceHolderStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KtUnionType : KtElementImplStub<KotlinPlaceHolderStub<KtUnionType>>, KtTypeElement {
    constructor(node: ASTNode) : super(node)
    constructor(stub: KotlinPlaceHolderStub<KtUnionType>) : super(stub, KtStubElementTypes.UNION_TYPE)

    override fun getTypeArgumentsAsTypes(): List<KtTypeReference> {
        return getStubOrPsiChildren(KtStubElementTypes.TYPE_ELEMENT_TYPES, KtTypeElement.ARRAY_FACTORY)
            .flatMap { it.typeArgumentsAsTypes }
    }

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R {
        return visitor.visitUnionType(this, data)
    }
}