# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

##################################################################################################
# Gson specific rules - Required for JSON deserialization
##################################################################################################

# Gson uses generic type information stored in a class file when working with fields.
# Keep attributes required for proper Gson functionality.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Prevent obfuscation of classes that Gson uses reflection on
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Keep UpdateInfo data class and all its fields from being obfuscated
# This is critical for Gson deserialization to work properly
-keep class com.example.onyx.Profile_Page$UpdateInfo { *; }
-keepclassmembers class com.example.onyx.Profile_Page$UpdateInfo { *; }

# Keep all data classes (Kotlin data classes used for JSON)
# This prevents "Abstract class can't be instantiated" errors
-keep class * implements java.io.Serializable { *; }
-keepclassmembers class * implements java.io.Serializable { *; }

# Keep all model classes that might be used with Gson
# Add more specific rules here if you have other data classes
-keep class com.example.onyx.Database.** { *; }
-keepclassmembers class com.example.onyx.Database.** { *; }

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Kotlin metadata for data classes
-keep class kotlin.Metadata { *; }

##################################################################################################
# End of Gson rules
##################################################################################################