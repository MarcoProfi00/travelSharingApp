# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep class for location to not show warning when building
-keep class com.google.android.gms.internal.location.zze { *; }
-keep class com.google.android.gms.internal.location.zze$* { *; }
-keepclassmembers class com.google.android.gms.internal.location.zze$* { *; }

-dontwarn com.google.android.gms.internal.location.zze

# Keep all public constructors of classes extending ViewModel.
-keep public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Keep all classes and their members in the data model package
-keep class com.example.travelsharingapp.data.model.** { *; }