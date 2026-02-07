# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========================================
# Retrofit & OkHttp
# ========================================
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okio.**
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ========================================
# Gson - Keep model classes for serialization
# ========================================
-keepattributes EnclosingMethod
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep all model classes (prevent obfuscation of field names)
-keep class com.shivams.mockmate.model.** { *; }
-keep class com.shivams.mockmate.model.analysis.** { *; }

# ========================================
# Room Database
# ========================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.shivams.mockmate.data.database.**Entity { *; }
-keep class com.shivams.mockmate.data.database.**Dao { *; }

# ========================================
# Firebase
# ========================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ========================================
# Hilt / Dagger
# ========================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ========================================
# Compose
# ========================================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ========================================
# Kotlin Coroutines
# ========================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========================================
# Vico Charts
# ========================================
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ========================================
# Data classes used in API responses
# ========================================
-keep class com.shivams.mockmate.model.GeminiRequest { *; }
-keep class com.shivams.mockmate.model.GeminiResponse { *; }
-keep class com.shivams.mockmate.model.Content { *; }
-keep class com.shivams.mockmate.model.Part { *; }
-keep class com.shivams.mockmate.model.GenerationConfig { *; }
-keep class com.shivams.mockmate.model.Candidate { *; }