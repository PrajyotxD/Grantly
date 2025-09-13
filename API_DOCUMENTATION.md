# Grantly SDK - API Documentation

## Overview

This document provides comprehensive documentation for all public APIs in the Grantly Android Permission Management SDK.

## Core Classes

### Grantly

Main entry point for the SDK. Provides static methods for creating permission requests and configuring global settings.

**Static Methods:**
- `requestPermissions(Activity)` - Create permission request for Activity
- `requestPermissions(Fragment)` - Create permission request for Fragment  
- `configure(GrantlyConfig)` - Set global SDK configuration
- `getConfig()` - Get current global configuration
- `initialize(Context)` - Initialize SDK with default config
- `reset()` - Reset SDK to default state (testing only)
- `isInitialized()` - Check if SDK is initialized
- `cleanup()` - Cleanup SDK resources
- `onRequestPermissionsResult(int, String[], int[])` - Handle permission results

### PermissionRequest

Builder class for configuring and executing permission requests with fluent API.

**Methods:**
- `permissions(String...)` - Set permissions to request
- `addPermissions(String...)` - Add additional permissions
- `setLazy(boolean)` - Enable/disable lazy mode
- `setRationale(String, String)` - Set simple rationale with title and message
- `setRationale(RationaleProvider)` - Set custom rationale provider
- `setCustomDialog(DialogProvider)` - Set custom dialog provider
- `setCustomDialog(@LayoutRes int)` - Set custom dialog layout
- `setContinueOnDenied(boolean)` - Set behavior when permissions denied
- `setCallbacks(GrantlyCallback)` - Set result callback
- `setDenialBehavior(DenialBehavior)` - Set denial behavior
- `execute()` - Execute the permission request

### GrantlyConfig

Immutable configuration class for global SDK settings.

**Methods:**
- `isDefaultLazyMode()` - Get default lazy mode setting
- `getDefaultDenialBehavior()` - Get default denial behavior
- `getCustomUiProvider()` - Get custom UI provider
- `isLoggingEnabled()` - Check if logging is enabled
- `getDefaultDialogTheme()` - Get default dialog theme
- `getDefaultToastTheme()` - Get default toast theme
- `shouldShowRationaleByDefault()` - Check if rationale shown by default
- `getDefaultRationaleTitle()` - Get default rationale title
- `getDefaultRationaleMessage()` - Get default rationale message

**Static Methods:**
- `builder()` - Create new Builder instance
- `getDefault()` - Get default configuration

### GrantlyConfig.Builder

Builder for creating GrantlyConfig instances.

**Methods:**
- `setDefaultLazyMode(boolean)` - Set default lazy mode
- `setDefaultDenialBehavior(DenialBehavior)` - Set default denial behavior
- `setCustomUiProvider(CustomUiProvider)` - Set custom UI provider
- `setLoggingEnabled(boolean)` - Enable/disable logging
- `setDefaultDialogTheme(@StyleRes int)` - Set default dialog theme
- `setDefaultToastTheme(@StyleRes int)` - Set default toast theme
- `setShowRationaleByDefault(boolean)` - Set default rationale behavior
- `setDefaultRationaleTitle(String)` - Set default rationale title
- `setDefaultRationaleMessage(String)` - Set default rationale message
- `build()` - Build GrantlyConfig instance

## Callback Interfaces

### GrantlyCallback

Interface for handling permission request results.

**Methods:**
- `onPermissionGranted(String[])` - Called when permissions granted
- `onPermissionDenied(String[])` - Called when permissions denied
- `onPermissionPermanentlyDenied(String[])` - Called when permanently denied
- `onPermissionRequestCancelled()` - Called when request cancelled (default empty)
- `onPermissionResult(List<PermissionResult>)` - Called with detailed results

## Provider Interfaces

### DialogProvider

Interface for custom permission dialog implementations.

**Methods:**
- `showPermissionDialog(Context, String[], DialogCallback)` - Show custom permission dialog

### RationaleProvider

Interface for custom rationale dialog implementations.

**Methods:**
- `showRationale(Context, String[], RationaleCallback)` - Show custom rationale dialog

### ToastProvider

Interface for custom toast message implementations.

**Methods:**
- `showToast(Context, String, int)` - Show custom toast message

### CustomUiProvider

Interface for providing custom UI implementations.

**Methods:**
- `getDialogProvider()` - Get custom dialog provider
- `getRationaleProvider()` - Get custom rationale provider  
- `getToastProvider()` - Get custom toast provider

## Utility Classes

### GrantlyUtils

Utility class with helper methods for permission management.

**Static Methods:**
- `isPermissionDeclared(Context, String)` - Check if permission declared in manifest
- `openAppSettings(Context)` - Open app settings for manual permission grant
- `isPermanentlyDenied(Activity, String[])` - Check if permissions permanently denied
- `getDangerousPermissions(Context)` - Get all dangerous permissions from manifest
- `isSpecialPermission(String)` - Check if permission requires special handling
- `getSpecialPermissionIntent(String)` - Get intent for special permission request
- `getPermissionDisplayName(String)` - Get user-friendly permission name
- `getPermissionDescription(String)` - Get permission description
- `getPermissionIconResource(String)` - Get permission icon resource

## Enums

### DenialBehavior

Enum defining behavior when permissions are denied.

**Values:**
- `CONTINUE_APP_FLOW` - Continue normal app flow
- `DISABLE_FEATURE` - Disable specific features
- `EXIT_APP_WITH_DIALOG` - Exit app with explanation dialog
- `EXIT_APP_IMMEDIATELY` - Exit app immediately

### PermissionState

Enum representing permission states.

**Values:**
- `GRANTED` - Permission is granted
- `DENIED` - Permission is denied but can be requested again
- `PERMANENTLY_DENIED` - Permission is permanently denied
- `NOT_DECLARED` - Permission not declared in manifest
- `REQUIRES_SPECIAL_HANDLING` - Permission requires special handling

## Model Classes

### PermissionResult

Data class containing detailed permission request results.

**Methods:**
- `getPermission()` - Get permission string
- `getState()` - Get permission state
- `isGranted()` - Check if permission is granted
- `isDenied()` - Check if permission is denied
- `isPermanentlyDenied()` - Check if permission is permanently denied
- `requiresSpecialHandling()` - Check if permission requires special handling
- `getTimestamp()` - Get result timestamp
- `getRequestId()` - Get request ID

## Exception Classes

### GrantlyException

Base exception class for all Grantly SDK exceptions.

**Constructors:**
- `GrantlyException(String)` - Create with message
- `GrantlyException(String, Throwable)` - Create with message and cause

### PermissionNotDeclaredException

Exception thrown when requesting undeclared permissions.

**Methods:**
- `getUndeclaredPermissions()` - Get array of undeclared permissions

### PermissionRequestInProgressException

Exception thrown when another permission request is already in progress.

**Methods:**
- `getActiveRequestId()` - Get ID of active request

### InvalidConfigurationException

Exception thrown when SDK configuration is invalid.

**Methods:**
- `getConfigurationIssue()` - Get description of configuration issue

## Thread Safety

- **Grantly**: Thread-safe, can be called from any thread
- **PermissionRequest**: Not thread-safe, use from main thread only
- **GrantlyConfig**: Immutable and thread-safe once built
- **GrantlyConfig.Builder**: Not thread-safe
- **GrantlyUtils**: Thread-safe static methods
- **Callbacks**: Always called on main/UI thread

## API Levels

- **Minimum SDK**: API 23 (Android 6.0)
- **Target SDK**: API 34 (Android 14)
- **Special Permissions**: Various API level requirements
  - `SYSTEM_ALERT_WINDOW`: API 23+
  - `WRITE_SETTINGS`: API 23+
  - `REQUEST_INSTALL_PACKAGES`: API 26+
  - `MANAGE_EXTERNAL_STORAGE`: API 30+
  - `ACCESS_BACKGROUND_LOCATION`: API 29+
  - `POST_NOTIFICATIONS`: API 33+

## Best Practices

1. **Always handle all callback methods** - Implement all required callback methods
2. **Request permissions contextually** - Use lazy mode when possible
3. **Provide clear rationales** - Explain why permissions are needed
4. **Handle permanent denials** - Guide users to app settings
5. **Configure globally** - Set up defaults in Application class
6. **Test on multiple API levels** - Ensure compatibility across Android versions
7. **Validate manifest declarations** - Ensure all permissions are declared
8. **Use appropriate denial behaviors** - Choose behavior that fits your app's UX

## Migration Guide

### From Other Libraries

When migrating from other permission libraries:

1. **Remove old dependencies** from build.gradle
2. **Replace permission request calls** with Grantly API
3. **Update callback implementations** to use GrantlyCallback
4. **Configure global settings** in Application class
5. **Test permission flows** on different Android versions

### Version Updates

When updating Grantly SDK versions:

1. **Check changelog** for breaking changes
2. **Update ProGuard rules** if needed
3. **Test critical permission flows**
4. **Update documentation references**

## Troubleshooting

### Common Issues

1. **PermissionNotDeclaredException**: Add permission to AndroidManifest.xml
2. **PermissionRequestInProgressException**: Wait for current request to complete
3. **InvalidConfigurationException**: Check GrantlyConfig builder parameters
4. **Callbacks not called**: Ensure Activity implements onRequestPermissionsResult
5. **Special permissions not working**: Check API level requirements

### Debug Logging

Enable logging in debug builds:

```java
GrantlyConfig config = GrantlyConfig.builder()
    .setLoggingEnabled(BuildConfig.DEBUG)
    .build();
Grantly.configure(config);
```

### ProGuard Issues

Ensure ProGuard rules are properly configured:

```proguard
-keep class dev.grantly.px.** { *; }
-keep interface dev.grantly.px.** { *; }
```