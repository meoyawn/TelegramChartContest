-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
-dontpreverify
-verbose

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
}

# MOSHI
-dontwarn okio.**
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# Enum field names are used by the integrated EnumJsonAdapter.
# Annotate enums with @JsonClass(generateAdapter = false) to use them with Moshi.
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
}

# FLEXBOX
-dontwarn com.google.android.flexbox.**
