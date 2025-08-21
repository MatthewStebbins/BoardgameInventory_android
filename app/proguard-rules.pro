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

# Glide - Comprehensive rules for production
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# For DexGuard only
# -keepresourcexmlelements manifest/application/meta-data@value=GlideModule
-dontwarn com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn com.bumptech.glide.manager.RequestManagerRetriever

# OpenCSV - Complete rules for production
-keep class com.opencsv.** { *; }
-keepattributes InnerClasses
# Needed for reflection used in OpenCSV
-keepclassmembers class * {
    @com.opencsv.bean.CsvBindByName *;
    @com.opencsv.bean.CsvCustomBindByName *;
    @com.opencsv.bean.CsvBindByPosition *;
    @com.opencsv.bean.CsvCustomBindByPosition *;
    @com.opencsv.bean.CsvBindAndSplitByName *;
    @com.opencsv.bean.CsvBindAndSplitByPosition *;
    @com.opencsv.bean.CsvDate *;
    @com.opencsv.bean.CsvNumber *;
    @com.opencsv.bean.CsvIgnore *;
}
-dontwarn com.opencsv.**
-dontwarn com.sun.beans.**
-dontwarn java.beans.**

# Apache POI - Comprehensive rules for Excel file handling
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.schemas.** { *; }
-keep class org.etsi.uri.x01903.v13.** { *; }
-keep class org.w3.x2000.x09.xmldsig.** { *; }
-keep class schemaorg_apache_xmlbeans.system.** { *; }
-keep class schemasMicrosoftComOfficeExcel.** { *; }
-keep class schemasMicrosoftComOfficeOffice.** { *; }

# Specific Apache POI warnings to ignore
-dontwarn org.apache.poi.**
-dontwarn org.apache.commons.collections4.**
-dontwarn org.apache.xmlbeans.**
-dontwarn schemaorg_apache_xmlbeans.**
-dontwarn org.w3c.dom.**
-dontwarn javax.xml.**
-dontwarn org.slf4j.**
-dontwarn org.apache.log4j.**

# Exclude the SVGUserAgent class from R8 processing
-dontwarn org.apache.poi.xslf.draw.SVGUserAgent
-keep class org.apache.poi.xslf.draw.SVGUserAgent { *; }

# Android support libraries
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep class com.google.android.material.** { *; }
-dontwarn androidx.**
-dontwarn com.google.android.material.**

# Keep ViewModels
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Ensure serialization libraries work properly
-keepattributes *Annotation*, Signature, Exception

# AdMob (important if using ads)
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# Keep enum fields for serialization
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep aQute.bnd.annotation classes
-keep class aQute.bnd.annotation.** { *; }

# Keep edu.umd.cs.findbugs.annotations classes
-keep class edu.umd.cs.findbugs.annotations.** { *; }

# Keep org.apache.logging.log4j classes
-keep class org.apache.logging.log4j.** { *; }

