# Grantly SDK Consumer ProGuard Rules
# These rules are automatically applied to apps that use the Grantly SDK

# Keep all public API classes and interfaces
-keep public class dev.grantly.px.Grantly { *; }
-keep public class dev.grantly.px.PermissionRequest { *; }
-keep public class dev.grantly.px.config.GrantlyConfig { *; }
-keep public class dev.grantly.px.config.GrantlyConfig$Builder { *; }

# Keep all callback interfaces and their implementations
-keep public interface dev.grantly.px.callback.GrantlyCallback { *; }
-keep public class * implements dev.grantly.px.callback.GrantlyCallback { *; }

# Keep all provider interfaces and their implementations
-keep public interface dev.grantly.px.provider.DialogProvider { *; }
-keep public interface dev.grantly.px.provider.RationaleProvider { *; }
-keep public interface dev.grantly.px.provider.ToastProvider { *; }
-keep public interface dev.grantly.px.provider.CustomUiProvider { *; }
-keep public class * implements dev.grantly.px.provider.** { *; }

# Keep utility classes
-keep public class dev.grantly.px.util.GrantlyUtils { *; }

# Keep model classes
-keep public class dev.grantly.px.model.** { *; }

# Keep enums
-keep public enum dev.grantly.px.config.DenialBehavior { *; }
-keep public enum dev.grantly.px.model.PermissionState { *; }

# Keep exception classes
-keep public class dev.grantly.px.exception.** { *; }

# Keep callback methods from being obfuscated
-keepclassmembers class * implements dev.grantly.px.callback.GrantlyCallback {
    public void onPermission*(...);
}

# Keep provider callback methods
-keepclassmembers class * implements dev.grantly.px.provider.** {
    public void *(...);
}

# Keep Android framework callback methods that might be called via reflection
-keepclassmembers class * extends android.app.Activity {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}

# Keep fragment callback methods
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep custom attributes for runtime access
-keepattributes *Annotation*

# Keep generic signatures for proper type erasure
-keepattributes Signature

# Keep inner classes
-keepattributes InnerClasses,EnclosingMethod

# Don't warn about missing classes that are not part of the SDK
-dontwarn dev.grantly.px.internal.**

# Keep resource identifiers
-keepclassmembers class **.R$* {
    public static <fields>;
}

# Keep manifest parser functionality
-keep class dev.grantly.px.core.ManifestParser { *; }
-keepclassmembers class dev.grantly.px.core.ManifestParser {
    *;
}

# Keep permission checker functionality
-keep class dev.grantly.px.core.PermissionChecker { *; }
-keepclassmembers class dev.grantly.px.core.PermissionChecker {
    *;
}

# Keep special permission handler
-keep class dev.grantly.px.core.SpecialPermissionHandler { *; }
-keepclassmembers class dev.grantly.px.core.SpecialPermissionHandler {
    *;
}

# Keep default implementations
-keep class dev.grantly.px.provider.impl.** { *; }

# Preserve method parameters for better debugging
-keepparameternames

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep builder pattern methods
-keepclassmembers class **.Builder {
    public <methods>;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}