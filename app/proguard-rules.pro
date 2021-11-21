## default & basic optimization configurations
-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes *Annotation*
-keepattributes LineNumberTable,SourceFile

-keep interface android.view.** { *; }
-keep class java.beans.** { *; }

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
# noinspection ShrinkerUnresolvedReference
-keep class com.squareup.okhttp.** { *; }
# noinspection ShrinkerUnresolvedReference
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

-dontwarn rx.**
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepclasseswithmembers class * {
    @retrofit.http.** <methods>;
}

-keep class sun.misc.Unsafe { *; }
# your package path where your gson models are stored
-keep interface za.co.xisystems.itis_rrm.data.localDB.dao.** { *; }
-keep class za.co.xisystems.itis_rrm.data.localDB.entities.** { *; }
-keep class za.co.xisystems.itis_rrm.data.localDB.views.** { *; }
-keep class za.co.xisystems.itis_rrm.data.network.responses.** { *; }
-keep class za.co.xisystems.itis_rrm.data.network.request.** { *; }
-keep class za.co.xisystems.itis_rrm.services.LocationValidation {*; }
-keep class za.co.xisystems.itis_rrm.forge.** { *;}
-keep class za.co.xisystems.itis_rrm.custom.** { *; }
-keepclasseswithmembers class za.co.xisystems.itis_rrm.domain.** { *; }

# Keep these for GSON and Jackson
-keepattributes Signature
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Annotation
-keepattributes *Annotation*

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
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Preserve the names of Serializable and Enum Objects
-keep class * implements java.io.Serializable { *;}
-keep enum za.co.xisystems.itis_rrm.** { *;}

-keep interface android.view.WindowInsetsController { *; }
-keep interface android.view.WindowInsetsAnimationControlListener { *; }
-keep interface android.view.WindowInsetsAnimation { *; }

-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn java.awt.color.ICC_Profile
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry

# kodein requirements
-keep, allowobfuscation, allowoptimization class org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.TypeReference
-keep, allowobfuscation, allowoptimization class * extends org.kodein.type.JVMAbstractTypeToken$Companion$WrappingTest

# --- AutoValue ---
# AutoValue annotations are retained but dependency is compileOnly.
-dontwarn com.google.auto.value.**

# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

# mapbox
-keep interface android.view.** { *; }
-keep class java.beans.** { *; }
-keep class com.google.auto.value.** { *;}

# more sqlcipher

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

-dontwarn javax.annotation.**

-dontwarn android.app.**
-dontwarn android.support.**
-dontwarn android.view.**
-dontwarn android.widget.**

-dontwarn com.google.common.primitives.**

-dontwarn **CompatHoneycomb
-dontwarn **CompatHoneycombMR2
-dontwarn **CompatCreatorHoneycombMR2

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class net.sqlcipher.** {
    *;
}
