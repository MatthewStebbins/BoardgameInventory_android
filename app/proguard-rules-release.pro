# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
# http://developer.android.com/guide/developing/tools/proguard.html

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep API classes
-keep class com.boardgameinventory.api.** { *; }
-keep class com.boardgameinventory.data.** { *; }
-keep class com.boardgameinventory.model.** { *; }

# Keep Room database classes
-keep class com.boardgameinventory.database.** { *; }

# Apache POI rules
-dontwarn org.etsi.**
-dontwarn org.openxmlformats.**
-dontwarn java.awt.**
-dontwarn org.w3.**
-dontwarn org.osgi.**

# Keep necessary classes for Excel handling
-keep class org.apache.poi.** { *; }

# SQLCipher rules
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# ZXing barcode scanner
-keep class com.google.zxing.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# AdMob rules
-keep class com.google.android.gms.ads.** { *; }

# Retrofit rules
-keepattributes Signature
-keepattributes Exceptions
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

# OkHttp rules
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson rules
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

# Glide rules
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.** { *; }

