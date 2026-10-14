# ═══════════════════════════════════════════════════════════════
# RecipeApp R8 rules
# Для стека: Kotlin Multiplatform + Compose + Ktor + Coil3 + Koin
#            + kotlinx.serialization + FileKit + Krop + Multiplatform-Settings
# ═══════════════════════════════════════════════════════════════

# ─── Kotlin Metadata ───
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ─── Kotlinx Serialization ───
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.appa.recipeapp.**$$serializer { *; }
-keepclassmembers class com.appa.recipeapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.appa.recipeapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Правила для DTO с @Serializable
-keep class com.appa.recipeapp.data.remote.dto.** { *; }
-keep class com.appa.recipeapp.data.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ─── Ktor ───
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
-dontwarn kotlinx.coroutines.debug.**

# Ktor OkHttp engine (для Android)
-keep class io.ktor.client.engine.okhttp.** { *; }
-dontwarn io.ktor.client.engine.okhttp.**

# Ktor Content Negotiation + JSON
-keep class io.ktor.serialization.kotlinx.json.** { *; }

# ─── OkHttp ───
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ─── Coil 3 ───
-keep class coil3.** { *; }
-keep interface coil3.** { *; }
-dontwarn coil3.**

# Coil network layer
-keep class coil3.network.** { *; }
-keep class coil3.network.ktor3.** { *; }

# Coil SVG/Decoder (если используется)
-keep class coil3.decode.** { *; }
-keep class coil3.request.** { *; }

# ─── Koin ───
-keep class org.koin.** { *; }
-keep interface org.koin.** { *; }
-dontwarn org.koin.**

# Koin Compose
-keep class org.koin.compose.** { *; }
-keep class org.koin.android.** { *; }

# ─── Kotlin Compose ───
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Compose runtime часто использует reflection — оставить
-keepclassmembers class androidx.compose.runtime.** {
    *;
}

# ─── FileKit ───
-keep class io.github.vinceglb.filekit.** { *; }
-dontwarn io.github.vinceglb.filekit.**

# ─── Krop (crop) ───
-keep class com.attafitamim.krop.** { *; }
-dontwarn com.attafitamim.krop.**

# ─── Multiplatform Settings ───
-keep class com.russhwolf.settings.** { *; }
-dontwarn com.russhwolf.settings.**

# ─── androidx Splashscreen ───
-keep class androidx.core.splashscreen.** { *; }

# ─── Kotlin общие правила ───
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Сохранить enum values() и valueOf() — R8 их иначе может вырезать
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Сохранить Parcelable CREATOR
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Сохранить имя MainActivity
-keep class com.appa.recipeapp.MainActivity { *; }

# ─── Убрать логи в release ───
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}