# Add project specific ProGuard rules here.

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If your code uses reflection, keep the classes that will be accessed through reflection
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes Exceptions

# Application classes that will be serialized/deserialized with Gson
-keep class com.boardgameinventory.data.** { *; }
-keep class com.boardgameinventory.api.models.** { *; }

# Room database
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static <fields>;
}
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Retrofit 2.x
-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ZXing (Barcode scanning)
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class com.journeyapps.barcodescanner.** { *; }
-dontwarn com.journeyapps.barcodescanner.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# OpenCSV
-keep class com.opencsv.** { *; }
-dontwarn com.opencsv.**

# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
-keep class org.openxmlformats.** { *; }
-keep class org.xml.** { *; }
-dontwarn org.w3c.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Google AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# AndroidX
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep ViewModels
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep View Binding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# Keep enum names
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove Log statements in release build
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
