## default & basic optimization configurations
-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*

-verbose

-printseeds obfuscation/seeds.txt ## all the classes and dependencies we actually use
-printusage obfuscation/unused.txt ## unused classes that are stripped out in the process
-printmapping obfuscation/mapping.txt ## mapping file that shows the obfuscated names of the classes after proguard is applied

## the developer can specify keywords for the obfuscation (I'm using Pokemon names)
-obfuscationdictionary obfuscation/keywords.txt
-classobfuscationdictionary obfuscation/keywords.txt
-packageobfuscationdictionary obfuscation/keywords.txt

# retrofit
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
#noinspection ShrinkerUnresolvedReference
-keep class com.squareup.okhttp.** { *; }
#noinspection ShrinkerUnresolvedReference
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

-keep class sun.misc.Unsafe { *; }
#your package path where your gson models are stored
-keep class za.co.xisystems.itis_rrm.data.localDB.entities.** { *; }
-keep class za.co.xisystems.itis_rrm.data.localDB.dao.** {*; }

# Keep these for GSON and Jackson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# SQLCipher
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

# OkHttp3
-keep class okhttp3.** { *; }
#noinspection ShrinkerUnresolvedReference,ShrinkerUnresolvedReference
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Needed for Parcelable/SafeParcelable Creators to not get stripped
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}