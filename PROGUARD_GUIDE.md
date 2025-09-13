# Grantly SDK - ProGuard Configuration Guide

## Overview

This guide provides comprehensive information about ProGuard configuration for the Grantly Android Permission Management SDK.

## Automatic Configuration

The Grantly SDK includes consumer ProGuard rules that are automatically applied to your app when you include the library. No manual configuration is required for basic usage.

## Consumer ProGuard Rules

The following rules are automatically applied to apps using Grantly SDK:

```proguard
# Grantly SDK Consumer ProGuard Rules
-keep public class dev.grantly.px.Grantly { *; }
-keep public class dev.grantly.px.PermissionRequest { *; }
-keep public class dev.grantly.px.config.GrantlyConfig { *; }
-keep public class dev.grantly.px.config.GrantlyConfig$Builder { *; }

# Keep all callback interfaces and implementations
-keep public interface dev.grantly.px.callback.GrantlyCallback { *; }
-keep public class * implements dev.grantly.px.callback.GrantlyCallback { *; }

# Keep all provider interfaces and implementations
-keep public interface dev.grantly.px.provider.** { *; }
-keep public class * implements dev.grantly.px.provider.** { *; }

# Keep utility classes
-keep public class dev.grantly.px.util.GrantlyUtils { *; }

# Keep model classes and enums
-keep public class dev.grantly.px.model.** { *; }
-keep public enum dev.grantly.px.config.DenialBehavior { *; }
-keep public enum dev.grantly.px.model.PermissionState { *; }

# Keep exception classes
-keep public class dev.grantly.px.exception.** { *; }
```

## Manual Configuration (Optional)

If you need to add additional rules or customize the configuration, add these rules to your app's `proguard-rules.pro` file:

### Basic Rules

```proguard
# Grantly SDK - Basic Rules
-keep class dev.grantly.px.** { *; }
-keep interface dev.grantly.px.** { *; }

# Keep callback implementations
-keep class * implements dev.grantly.px.callback.GrantlyCallback { *; }
-keep class * implements dev.grantly.px.provider.** { *; }
```

### Advanced Rules

```proguard
# Grantly SDK - Advanced Rules

# Keep callback methods from being obfuscated
-keepclassmembers class * implements dev.grantly.px.callback.GrantlyCallback {
    public void onPermission*(...);
}

# Keep provider callback methods
-keepclassmembers class * implements dev.grantly.px.provider.** {
    public void *(...);
}

# Keep Android framework callback methods
-keepclassmembers class * extends android.app.Activity {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
}

-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void onRequestPermissionsResult(int, java.lang.String[], int[]);
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
```

### Custom Implementation Rules

If you implement custom providers or callbacks:

```proguard
# Custom Grantly implementations
-keep class com.yourpackage.** implements dev.grantly.px.provider.** { *; }
-keep class com.yourpackage.** implements dev.grantly.px.callback.** { *; }

# Keep custom dialog implementations
-keep class com.yourpackage.dialogs.** extends android.app.Dialog { *; }
-keep class com.yourpackage.dialogs.** extends androidx.appcompat.app.AlertDialog { *; }

# Keep custom view implementations
-keep class com.yourpackage.views.** extends android.view.View { *; }
```

## R8 Configuration

If you're using R8 (default in Android Gradle Plugin 3.4.0+), the same rules apply. R8 is more aggressive than ProGuard, so ensure you test thoroughly.

### R8-Specific Rules

```proguard
# R8 specific rules for Grantly SDK
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature

# Keep reflection-based access
-keepclassmembers class dev.grantly.px.** {
    @dev.grantly.px.annotation.** *;
}

# Keep generic signatures
-keepattributes Signature
```

## Troubleshooting

### Common Issues

1. **Callbacks not working after obfuscation**
   ```proguard
   -keep class * implements dev.grantly.px.callback.GrantlyCallback { *; }
   ```

2. **Custom providers not found**
   ```proguard
   -keep class com.yourpackage.** implements dev.grantly.px.provider.** { *; }
   ```

3. **Permission request results not handled**
   ```proguard
   -keepclassmembers class * extends android.app.Activity {
       public void onRequestPermissionsResult(int, java.lang.String[], int[]);
   }
   ```

4. **Enum values not accessible**
   ```proguard
   -keepclassmembers enum dev.grantly.px.** {
       public static **[] values();
       public static ** valueOf(java.lang.String);
   }
   ```

### Debug Configuration

For debugging ProGuard issues:

```proguard
# Debug configuration
-keepattributes SourceFile,LineNumberTable
-keepparameternames
-renamesourcefileattribute SourceFile

# Print configuration
-printconfiguration build/outputs/mapping/configuration.txt
-printmapping build/outputs/mapping/mapping.txt
-printusage build/outputs/mapping/usage.txt
-printseeds build/outputs/mapping/seeds.txt
```

### Testing ProGuard Configuration

1. **Build release APK with ProGuard enabled**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Test all permission flows**
   - Basic permission requests
   - Custom dialogs and rationales
   - Special permissions
   - Error handling

3. **Check mapping files**
   - Review `build/outputs/mapping/mapping.txt`
   - Ensure critical classes are not obfuscated

4. **Test on different devices and API levels**
   - Android 6.0+ (API 23+)
   - Different OEM customizations

## Build Configuration

### Gradle Configuration

Ensure your app's `build.gradle` has proper ProGuard configuration:

```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Library Module Configuration

The Grantly SDK library module uses this configuration:

```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            consumerProguardFiles "consumer-rules.pro"
        }
    }
}
```

## Best Practices

1. **Always test with ProGuard enabled** - Test your app thoroughly with ProGuard/R8 enabled before release

2. **Use consumer ProGuard rules** - The SDK provides automatic rules, but add custom rules for your implementations

3. **Keep debugging information** - Include source file and line number information for better crash reports

4. **Test on multiple devices** - Different Android versions and OEMs may behave differently

5. **Monitor crash reports** - Watch for ProGuard-related crashes in production

6. **Use mapping files** - Keep mapping files for each release to deobfuscate crash reports

## Advanced Configuration

### Custom Annotations

If you use custom annotations with Grantly:

```proguard
-keepattributes *Annotation*
-keep @interface com.yourpackage.annotations.**
-keepclassmembers class * {
    @com.yourpackage.annotations.** *;
}
```

### Reflection Usage

If you use reflection with Grantly APIs:

```proguard
-keepclassmembers class dev.grantly.px.** {
    public <methods>;
    public <fields>;
}
```

### Serialization Support

If you serialize Grantly model classes:

```proguard
-keepclassmembers class dev.grantly.px.model.** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
```

## Verification

### Verify ProGuard Rules

1. **Check consumer rules are applied**
   ```bash
   ./gradlew :app:assembleRelease --info | grep -i proguard
   ```

2. **Verify AAR contains consumer rules**
   ```bash
   unzip -l app/build/intermediates/library_and_local_jars_jni/release/classes.jar | grep proguard
   ```

3. **Test permission flows**
   - Install release APK
   - Test all permission scenarios
   - Verify callbacks work correctly

### Common Verification Steps

1. ✅ Basic permission requests work
2. ✅ Custom dialogs display correctly
3. ✅ Rationale messages show properly
4. ✅ Callbacks are invoked correctly
5. ✅ Special permissions handled properly
6. ✅ Error handling works as expected
7. ✅ Configuration changes handled correctly
8. ✅ No crashes in production builds

## Support

If you encounter ProGuard-related issues with Grantly SDK:

1. **Check the troubleshooting section** above
2. **Review your custom ProGuard rules**
3. **Test with minimal ProGuard configuration**
4. **Check the mapping files** for obfuscated class names
5. **Create an issue** with your ProGuard configuration and error details

## Version Compatibility

- **Grantly SDK 1.0.0+**: Supports ProGuard and R8
- **Android Gradle Plugin 3.4.0+**: R8 is used by default
- **Android Gradle Plugin 3.3.x and below**: ProGuard is used

Make sure your ProGuard rules are compatible with your Android Gradle Plugin version.