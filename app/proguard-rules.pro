# ProGuard rules for PredictX Sports Android
# P2-2：移除未使用依賴 (DataStore) 的 keep 規則，縮小 R8 keep 集合。

# ==========================================
# Kotlin
# ==========================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlin.** -dontwarn kotlin.**
-keepclassmembers class kotlin.Metadata { *; }

# ==========================================
# kotlinx.serialization
# ==========================================
-keepclassmembers class * extends kotlinx.serialization.Serializable { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep,includedescriptorclasses class com.predictxsports.android.**$$serializer { *; }
-keepclassmembers class com.predictxsports.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.predictxsports.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==========================================
# Retrofit + OkHttp
# ==========================================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers class com.predictxsports.android.data.remote.** { *; }

# ==========================================
# Google Play Billing
# ==========================================
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ==========================================
# Data classes (API models)
# ==========================================
-keep class com.predictxsports.android.data.model.** { *; }

# ==========================================
# Compose
# ==========================================
-dontwarn androidx.compose.**

# ==========================================
# General
# ==========================================
-keepnames class * { @kotlinx.serialization.Serializable *; }
-keepattributes SourceFile,LineNumberTable
-keep class kotlin.Metadata { *; }
