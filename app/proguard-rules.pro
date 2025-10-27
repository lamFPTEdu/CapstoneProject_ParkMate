-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Tối ưu hóa để tránh ANR
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# Keep classes để tránh crash
-keep class com.parkmate.android.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# MapLibre
-keep class org.maplibre.** { *; }
-dontwarn org.maplibre.**

# RxJava
-keep class io.reactivex.** { *; }
-dontwarn io.reactivex.**

# OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

