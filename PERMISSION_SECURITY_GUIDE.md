# Permission System Security and Debugging Guide

## Overview

This document provides guidelines for safely using the Grantly permission system and debugging permission-related issues, especially the reported issue where permission denials in debug mode appeared to affect other apps.

## Security Measures Implemented

### 1. Permission Isolation Manager

The `PermissionIsolationManager` class provides comprehensive protection against system-wide interference:

- **Request Validation**: All permission requests are validated before system calls
- **Rate Limiting**: Prevents system overload with timing constraints  
- **Package Verification**: Ensures operations only affect our own app
- **System Recovery**: Automatic recovery mode if anomalies detected

### 2. Enhanced Special Permission Handling

All Settings intents now explicitly specify package URIs:

```java
// Before (potentially unsafe)
Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);

// After (safe)  
Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
String ourPackage = activity.getPackageName();
Uri uri = Uri.parse("package:" + ourPackage);
intent.setData(uri);
```

### 3. System Integrity Monitoring

Regular integrity checks ensure the permission system remains healthy:

- Validates package access permissions
- Monitors for unexpected permission state changes  
- Detects system anomalies early
- Provides detailed logging for debugging

## Debugging Permission Issues

### Enable Debug Logging

In your Application class:

```java
@Override
public void onCreate() {
    super.onCreate();
    
    GrantlyConfig config = new GrantlyConfig.Builder()
        .setLoggingEnabled(BuildConfig.DEBUG) // Enable in debug builds
        .build();
        
    Grantly.configure(config);
}
```

### Monitor Permission Request Flow

The enhanced logging provides detailed information:

```
=== PERMISSION RESULT DEBUG START ===
onRequestPermissionsResult called:
  - Request code: 1001
  - Our package: dev.grantly.grantly.demo
  - Permissions: [android.permission.CAMERA]
  - Grant results: [0]
  - Permission details:
    * android.permission.CAMERA -> GRANTED
  - System info:
    * Android version: 33
    * Device manufacturer: Google
    * Device model: Pixel 7
  - Permission integrity check: PASS
  - Handled by Grantly: true
=== PERMISSION RESULT DEBUG END ===
```

### Check System Status

Get current isolation manager status:

```java
PermissionManager manager = Grantly.getPermissionManager();
PermissionIsolationManager isolation = manager.getIsolationManager();
String status = isolation.getSystemStatus();
Log.d("Grantly", "System Status: " + status);
```

## Troubleshooting Common Issues

### Issue: Permissions Denied Unexpectedly

**Symptoms**: Permissions that should be granted are denied

**Debugging Steps**:
1. Check integrity status: `isolation.performSystemIntegrityCheck()`
2. Verify package name: Ensure it matches expected value
3. Check for concurrent requests: Multiple requests can cause confusion
4. Review system logs for SecurityExceptions

**Solution**: 
- Use lazy permission requests to avoid conflicts
- Ensure proper error handling in callbacks
- Check for OEM-specific permission behaviors

### Issue: Settings Intents Not Working

**Symptoms**: Settings screens don't open or show wrong app

**Debugging Steps**:
1. Verify package URI format: `package:your.package.name`
2. Check for SecurityExceptions in logs
3. Test on different Android versions
4. Verify permission is declared in manifest

**Solution**:
- Always specify package URI in Settings intents
- Add try-catch around Settings intent launches
- Provide fallback to general settings if needed

### Issue: System Recovery Mode Triggered

**Symptoms**: All permission requests are blocked temporarily

**Debugging Steps**:
1. Check isolation manager status
2. Look for recovery trigger reasons in logs
3. Verify no system-level exceptions occurred
4. Check for memory/resource constraints

**Solution**:
- Wait for recovery timeout (5 seconds)
- Investigate root cause from logs  
- Ensure proper cleanup of permission requests
- Avoid rapid-fire permission requests

## Best Practices for Safe Permission Handling

### 1. Use Lazy Permission Requests

Request permissions when needed, not upfront:

```java
// Good: Request when user needs feature
private void takePhoto() {
    Grantly.requestPermissions(this)
        .permissions(Manifest.permission.CAMERA)
        .setLazy(true)
        .execute();
}

// Avoid: Requesting all permissions upfront
private void requestAllPermissions() {
    Grantly.requestPermissions(this)
        .permissions(/* 10+ permissions */)
        .execute();
}
```

### 2. Handle All Permission States

Always implement comprehensive callbacks:

```java
.setCallbacks(new GrantlyCallback() {
    @Override
    public void onPermissionGranted(String[] permissions) {
        // Enable feature
    }
    
    @Override
    public void onPermissionDenied(String[] permissions) {
        // Gracefully disable feature
    }
    
    @Override
    public void onPermissionPermanentlyDenied(String[] permissions) {
        // Guide user to settings
        GrantlyUtils.openAppSettings(activity);
    }
    
    @Override
    public void onPermissionRequestCancelled() {
        // Handle cancellation gracefully
    }
})
```

### 3. Avoid Concurrent Requests

Only make one permission request at a time:

```java
// Good: Sequential requests
private void requestPermissionsSequentially() {
    if (needsCameraPermission()) {
        requestCameraPermission(); // Handle in callback
    } else if (needsLocationPermission()) {
        requestLocationPermission();
    }
}

// Avoid: Multiple simultaneous requests
private void requestMultiplePermissions() {
    requestCameraPermission();   // Don't do this
    requestLocationPermission(); // at the same time
}
```

### 4. Test on Multiple Devices and OS Versions

Different manufacturers and Android versions may behave differently:

- Test on stock Android (Pixel devices)
- Test on Samsung devices (One UI)
- Test on Xiaomi devices (MIUI) 
- Test on OnePlus devices (OxygenOS)
- Test on different Android API levels (23, 28, 29, 31, 33+)

### 5. Monitor System Health

Periodically check system integrity:

```java
// In your main activity or application class
private void checkPermissionSystemHealth() {
    if (Grantly.isInitialized()) {
        PermissionManager manager = Grantly.getPermissionManager();
        boolean healthy = manager.getIsolationManager().performSystemIntegrityCheck();
        
        if (!healthy) {
            Log.w("App", "Permission system health check failed - investigating");
            // Log additional debugging information
        }
    }
}
```

## Reporting Permission Issues

When reporting permission-related issues, please include:

1. **Device Information**: Manufacturer, model, Android version
2. **App Information**: Debug/release build, package name
3. **Permission Details**: Which permissions were requested
4. **System Logs**: Relevant Logcat output with Grantly tags
5. **Isolation Status**: Output from `isolation.getSystemStatus()`
6. **Reproduction Steps**: Exact steps to reproduce the issue
7. **Other Apps Affected**: Specific apps and permissions affected

## Conclusion

The enhanced security measures in Grantly should prevent any system-wide permission interference. However, if you still experience issues:

1. Enable debug logging and collect detailed information
2. Follow the troubleshooting steps above
3. Report the issue with comprehensive debugging data
4. Consider device-specific workarounds if needed

The permission system isolation ensures that Grantly can only affect permissions for its own application, preventing any interference with other apps' permission states.