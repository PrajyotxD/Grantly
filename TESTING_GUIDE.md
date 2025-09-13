# Testing Scenarios for Permission Security

This document outlines specific test scenarios to validate that the Grantly SDK cannot interfere with other apps' permissions.

## Critical Security Tests

### Test 1: Isolated Permission Denial
**Objective**: Verify denying permissions in our app doesn't affect other apps

**Steps**:
1. Install test app with multiple dangerous permissions
2. Grant camera permission to WhatsApp, Instagram, and YouTube  
3. Launch Grantly demo app
4. Request camera permission and deny it (with "Don't ask again")
5. Verify other apps still have camera permission

**Expected Result**: Other apps retain their permissions

### Test 2: Concurrent App Permission Requests
**Objective**: Ensure permission requests don't interfere with other apps

**Steps**:
1. Launch Grantly demo app
2. Start requesting multiple permissions
3. Quickly switch to another app (WhatsApp) 
4. Try to use camera/microphone in WhatsApp
5. Return to Grantly app and complete permission flow

**Expected Result**: WhatsApp functionality unaffected

### Test 3: Settings Intent Isolation
**Objective**: Verify Settings intents only affect our app

**Steps**:
1. Grant overlay permission to multiple apps
2. Launch Grantly demo app
3. Request SYSTEM_ALERT_WINDOW permission
4. Observe Settings screen that opens
5. Verify it only shows our app's permission settings
6. Deny the permission
7. Check other apps still have overlay permission

**Expected Result**: Settings screen scoped to our app only

### Test 4: System Recovery Testing
**Objective**: Test system recovery doesn't affect other apps

**Steps**:
1. Force system recovery by triggering rapid permission requests
2. Verify recovery mode activates (check logs)
3. Wait for recovery timeout (5 seconds)
4. Check permission states of other apps
5. Resume normal permission requests

**Expected Result**: Other apps unaffected by recovery mode

### Test 5: Debug vs Release Mode
**Objective**: Ensure no differences between build types

**Steps**:
1. Run all above tests in debug build
2. Create release build of demo app
3. Run same tests in release build
4. Compare permission behaviors
5. Check for any debug-specific issues

**Expected Result**: Identical behavior in both modes

## Device-Specific Testing

### Samsung Devices (One UI)
- Test on Galaxy S series phones
- Check Samsung's permission manager compatibility
- Verify no interference with Secure Folder apps

### Xiaomi Devices (MIUI)
- Test on Redmi/Mi phones
- Check MIUI permission system compatibility
- Verify auto-start and background app permissions

### OnePlus Devices (OxygenOS)  
- Test on OnePlus phones
- Check OxygenOS permission behaviors
- Verify gaming mode doesn't affect permissions

### Stock Android (Pixel)
- Test on Google Pixel devices
- Verify baseline Android behavior
- Check adaptive permissions (Android 12+)

## Android Version Testing

### Android 6.0-8.1 (API 23-27)
- Basic runtime permission testing
- Legacy permission handling

### Android 9.0-10.0 (API 28-29)
- Foreground service permissions
- Background location permissions
- Scoped storage permissions

### Android 11+ (API 30+)
- One-time permissions
- Auto-reset permissions
- Permission rationale changes

### Android 12+ (API 31+)
- Approximate location permissions
- Hibernated apps
- Permission usage indicators

### Android 13+ (API 33+)
- Runtime notification permissions
- Photo picker permissions
- Enhanced permission dialogs

## Automated Test Scripts

### Shell Script for Permission Checking
```bash
#!/bin/bash
# check_permissions.sh

PACKAGE_LIST=(
    "com.whatsapp"
    "com.instagram.android" 
    "com.google.android.youtube"
    "dev.grantly.grantly.demo"
)

PERMISSION_LIST=(
    "android.permission.CAMERA"
    "android.permission.RECORD_AUDIO"
    "android.permission.ACCESS_FINE_LOCATION"
)

echo "=== Permission Check Before Test ==="
for package in "${PACKAGE_LIST[@]}"; do
    echo "Package: $package"
    for permission in "${PERMISSION_LIST[@]}"; do
        result=$(adb shell dumpsys package $package | grep "$permission" | grep "granted=true" || echo "DENIED")
        echo "  $permission: $result"
    done
    echo ""
done

echo "=== Run your test now, then press Enter ==="
read -r

echo "=== Permission Check After Test ==="
for package in "${PACKAGE_LIST[@]}"; do
    echo "Package: $package"  
    for permission in "${PERMISSION_LIST[@]}"; do
        result=$(adb shell dumpsys package $package | grep "$permission" | grep "granted=true" || echo "DENIED")
        echo "  $permission: $result"
    done
    echo ""
done
```

### ADB Commands for Manual Testing
```bash
# Check specific app permissions
adb shell dumpsys package com.whatsapp | grep permission

# Check all dangerous permissions for an app
adb shell pm list permissions -d -f | grep com.whatsapp

# Reset permissions for testing
adb shell pm reset-permissions dev.grantly.grantly.demo

# Grant permission for testing  
adb shell pm grant com.whatsapp android.permission.CAMERA

# Revoke permission for testing
adb shell pm revoke com.whatsapp android.permission.CAMERA
```

## Integration Tests

### Test with Popular Apps
Test permission interactions with commonly used apps:

1. **WhatsApp**: Camera, microphone, contacts, storage
2. **Instagram**: Camera, microphone, location, storage  
3. **YouTube**: Microphone, storage
4. **Google Maps**: Location, microphone
5. **Spotify**: Storage, microphone
6. **Camera apps**: Camera, storage

### Test Permission Groups
Verify behavior with different permission groups:

1. **Camera Group**: Camera permission
2. **Microphone Group**: Record audio permission
3. **Location Group**: Fine/coarse/background location
4. **Storage Group**: Read/write external storage
5. **Contacts Group**: Read/write contacts
6. **Phone Group**: Call phone, read phone state
7. **SMS Group**: Send/read SMS
8. **Calendar Group**: Read/write calendar

## Performance Testing

### Memory Usage
- Monitor memory usage during permission requests
- Check for memory leaks in isolation manager
- Verify cleanup of completed requests

### CPU Usage  
- Monitor CPU during permission flows
- Check for excessive background processing
- Verify efficient request handling

### Battery Impact
- Test battery usage during permission requests
- Check for wake locks or excessive processing
- Verify no background permission monitoring

## Security Validation

### Package Name Verification
```java
// Verify all Settings intents use our package
private void validateSettingsIntents() {
    String ourPackage = getPackageName();
    
    // Check overlay permission intent
    Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
    Uri overlayUri = overlayIntent.getData();
    assert overlayUri.toString().contains(ourPackage);
}
```

### Permission Scope Validation  
```java
// Verify we only check our own permissions
private void validatePermissionScope() {
    PackageManager pm = getPackageManager();
    
    try {
        // Should succeed - our own package
        pm.getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
        
        // Should not access other apps' detailed permission info
        // without proper permissions
    } catch (PackageManager.NameNotFoundException e) {
        // Expected for other packages
    }
}
```

## Reporting Test Results

When reporting test results, include:

1. **Test Environment**: Device model, Android version, build type
2. **Test Results**: Pass/fail for each scenario  
3. **Performance Data**: Memory, CPU, battery usage
4. **Permission States**: Before/after permission states for all apps
5. **System Logs**: Relevant Logcat output
6. **Screenshots**: Permission dialogs and Settings screens
7. **Anomalies**: Any unexpected behaviors observed

## Continuous Testing

### Automated Regression Testing
- Run critical security tests on every build
- Test on multiple device configurations
- Validate performance metrics stay within bounds

### Manual Validation  
- Weekly testing on latest Android versions
- Monthly testing on popular OEM devices
- Quarterly security audit of permission flows

This comprehensive testing approach ensures the Grantly SDK maintains proper permission isolation and cannot interfere with other applications' permission states.