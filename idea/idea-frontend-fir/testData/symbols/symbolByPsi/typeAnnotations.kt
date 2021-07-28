@Target(AnnotationTarget.TYPE)
annotation class Anno1
@Target(AnnotationTarget.TYPE)
annotation class Anno2
@Target(AnnotationTarget.TYPE)
annotation class Anno3
@Target(AnnotationTarget.TYPE)
annotation class Anno4

interface I

class X : @Anno1 I {
    fun f(arg: @Anno2 I): @Anno3 I = arg
    val x: @Anno4 I = this
}

// RESULT
/*
KtFirNamedClassOrObjectSymbol:
  annotationClassIds: [
    kotlin/annotation/Target
  ]
  annotations: [
    kotlin/annotation/Target(allowedTargets = KtUnsupportedConstantValue)
      psi: null
  ]
  classIdIfNonLocal: Anno1
  classKind: ANNOTATION_CLASS
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: FINAL
  name: Anno1
  origin: SOURCE
  superTypes: [
    [] kotlin/Annotation
  ]
  symbolKind: TOP_LEVEL
  typeParameters: []
  visibility: Public

KtFirNamedClassOrObjectSymbol:
  annotationClassIds: [
    kotlin/annotation/Target
  ]
  annotations: [
    kotlin/annotation/Target(allowedTargets = KtUnsupportedConstantValue)
      psi: null
  ]
  classIdIfNonLocal: Anno2
  classKind: ANNOTATION_CLASS
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: FINAL
  name: Anno2
  origin: SOURCE
  superTypes: [
    [] kotlin/Annotation
  ]
  symbolKind: TOP_LEVEL
  typeParameters: []
  visibility: Public

KtFirNamedClassOrObjectSymbol:
  annotationClassIds: [
    kotlin/annotation/Target
  ]
  annotations: [
    kotlin/annotation/Target(allowedTargets = KtUnsupportedConstantValue)
      psi: null
  ]
  classIdIfNonLocal: Anno3
  classKind: ANNOTATION_CLASS
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: FINAL
  name: Anno3
  origin: SOURCE
  superTypes: [
    [] kotlin/Annotation
  ]
  symbolKind: TOP_LEVEL
  typeParameters: []
  visibility: Public

KtFirNamedClassOrObjectSymbol:
  annotationClassIds: [
    kotlin/annotation/Target
  ]
  annotations: [
    kotlin/annotation/Target(allowedTargets = KtUnsupportedConstantValue)
      psi: null
  ]
  classIdIfNonLocal: Anno4
  classKind: ANNOTATION_CLASS
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: FINAL
  name: Anno4
  origin: SOURCE
  superTypes: [
    [] kotlin/Annotation
  ]
  symbolKind: TOP_LEVEL
  typeParameters: []
  visibility: Public

KtFirNamedClassOrObjectSymbol:
  annotationClassIds: []
  annotations: []
  classIdIfNonLocal: I
  classKind: INTERFACE
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: ABSTRACT
  name: I
  origin: SOURCE
  superTypes: [
    [] kotlin/Any
  ]
  symbolKind: TOP_LEVEL
  typeParameters: []
  visibility: Public

KtFirValueParameterSymbol:
  annotatedType: [
    Anno2()
      psi: null
  ] @R|Anno2|()  I
  annotationClassIds: []
  annotations: []
  callableIdIfNonLocal: null
  hasDefaultValue: false
  isExtension: false
  isVararg: false
  name: arg
  origin: SOURCE
  receiverType: null
  symbolKind: LOCAL

KtFirFunctionSymbol:
  annotatedType: [
    Anno3()
      psi: KtAnnotationEntry
  ] @R|Anno2|()  I
  annotationClassIds: []
  annotations: []
  callableIdIfNonLocal: /X.f
  dispatchType: X
  hasStableParameterNames: true
  isExtension: false
  isExternal: false
  isInfix: false
  isInline: false
  isOperator: false
  isOverride: false
  isStatic: false
  isSuspend: false
  modality: FINAL
  name: f
  origin: SOURCE
  receiverType: null
  symbolKind: MEMBER
  typeParameters: []
  valueParameters: [
    KtFirValueParameterSymbol(arg)
  ]
  visibility: Public

KtFirKotlinPropertySymbol:
  annotatedType: [
    Anno4()
      psi: KtAnnotationEntry
  ] @R|Anno2|()  I
  annotationClassIds: []
  annotations: []
  callableIdIfNonLocal: /X.x
  dispatchType: X
  getter: KtFirPropertyGetterSymbol(<getter>)
  hasBackingField: true
  hasGetter: true
  hasSetter: false
  initializer: KtUnsupportedConstantValue
  isConst: false
  isExtension: false
  isLateInit: false
  isOverride: false
  isStatic: false
  isVal: true
  modality: FINAL
  name: x
  origin: SOURCE
  receiverType: null
  setter: null
  symbolKind: MEMBER
  visibility: Public

KtFirNamedClassOrObjectSymbol:
  annotationClassIds: []
  annotations: []
  classIdIfNonLocal: X
  classKind: CLASS
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: FINAL
  name: X
  origin: SOURCE
  superTypes: [
    [
      Anno1()
        psi: null
    ] @R|Anno2|()  I
  ]
  symbolKind: TOP_LEVEL
  typeParameters: []
  visibility: Public
*/
