-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
-dontpreverify
-verbose

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
}
