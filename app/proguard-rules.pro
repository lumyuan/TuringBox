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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers public class * extends android.app.Activity
-keepclassmembers public class * extends android.app.Application
-keepclassmembers public class * extends android.app.Service
-keepclassmembers public class * extends android.content.BroadcastReceiver
-keepclassmembers class * extends java.io.Serializable{*;}
-keepclassmembers class * implements java.io.Serializable{*;}
#-keepclassmembers public class * extends android.content.ContentProvider
#-keepclassmembers public class * extends android.app.backup.BackupAgentHelper
#-keepclassmembers public class * extends android.preference.Preference
#-keepclassmembers public class * extends android.view.View
#-keepclassmembers public class com.android.vending.licensing.ILicensingService
#-keepclassmembers class android.support.** {*;}

-keep class io.github.lumyuan.turingbox.common.model.**{*;}