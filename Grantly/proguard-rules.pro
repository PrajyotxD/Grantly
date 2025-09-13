# Grantly SDK Internal ProGuard Rules
# These rules are applied when building the Grantly SDK library

# Keep all public APIs - these should not be obfuscated
-keep public class dev.grantly.px.** { public *; }

# Keep all interfaces - they might be implemented by consumer apps
-keep interface dev.grantly.px.** { *; }

# Keep internal core classes that are accessed via reflection or Android framework
-keep class dev.grantly.px.core.** { *; }

# Keep exception classes with their constructors and methods
-keep class dev.grantly.px.exception.** {
    <init>(...);
    public *;
}

# Keep model classes - they might be serialized or used in callbacks
-keep class dev.grantly.px.model.** {
    <init>(...);
    public *;
    private <fields>;
}

# Keep callback interfaces and ensure their methods are not renamed
-keep interface dev.grantly.px.callback.** { *; }

# Keep provider interfaces and their implementations
-keep interface dev.grantly.px.provider.** { *; }
-keep class dev.grantly.px.provider.impl.** { *; }

# Keep utility classes
-keep class dev.grantly.px.util.** { *; }

# Keep configuration classes
-keep class dev.grantly.px.config.** { *; }

# Keep logger class for debugging
-keep class dev.grantly.px.util.GrantlyLogger { *; }

# Keep Android framework integration points
-keepclassmembers class * extends android.app.Activity {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}

-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}

# Keep resource access
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep attributes for debugging and reflection
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep parameter names for better debugging
-keepparameternames

# Don't obfuscate enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

# Keep builder patterns
-keepclassmembers class * {
    public static *** builder();
    public *** build();
}

# Keep serialization support
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep parcelable support
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep classes with main methods
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# Optimization settings
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove logging in release builds (optional - can be enabled if needed)
# -assumenosideeffects class android.util.Log {
#     public static boolean isLoggable(java.lang.String, int);
#     public static int v(...);
#     public static int i(...);
#     public static int w(...);
#     public static int d(...);
#     public static int e(...);
# }

# Don't warn about missing classes that are not part of our SDK
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**

# Keep custom view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep onClick methods that might be referenced from XML
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# Keep fragment constructors
-keepclasseswithmembers class * extends androidx.fragment.app.Fragment {
    public <init>();
}

# Keep callback methods that might be called via reflection
-keepclassmembers class * {
    @dev.grantly.px.annotation.** *;
}

# Keep test classes if building with tests
-keep class **Test { *; }
-keep class **Tests { *; }
-keepclassmembers class **Test** { *; }

# Keep mock classes for testing
-keep class org.mockito.** { *; }
-keep class org.junit.** { *; }
-dontwarn org.mockito.**
-dontwarn org.junit.**