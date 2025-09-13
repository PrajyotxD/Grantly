package dev.grantly.px.core;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import dev.grantly.px.util.GrantlyLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages permission request isolation to prevent system-level interference.
 * This class ensures that permission requests are properly isolated and cannot
 * affect other applications' permissions or cause system-wide issues.
 */
public class PermissionIsolationManager {
    
    private static final String TAG = "PermissionIsolationManager";
    
    // Global request tracking to prevent system overload
    private static final ConcurrentHashMap<String, Long> globalRequestTracker = new ConcurrentHashMap<>();
    private static final AtomicLong lastSystemCallTime = new AtomicLong(0);
    private static final AtomicBoolean systemInRecovery = new AtomicBoolean(false);
    
    // Timing constraints to prevent system abuse
    private static final long MIN_REQUEST_INTERVAL_MS = 1000; // 1 second between requests
    private static final long MAX_CONCURRENT_REQUESTS = 3;
    private static final long REQUEST_TIMEOUT_MS = 30000; // 30 seconds
    private static final long RECOVERY_TIMEOUT_MS = 5000; // 5 seconds recovery time
    
    private final Context applicationContext;
    private final String packageName;
    
    /**
     * Creates a new PermissionIsolationManager.
     * 
     * @param context Application context
     */
    public PermissionIsolationManager(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.packageName = applicationContext.getPackageName();
        GrantlyLogger.d(TAG, "Initialized PermissionIsolationManager for package: " + packageName);
    }
    
    /**
     * Validates that a permission request is safe and won't cause system interference.
     * This method acts as a safety gate before any permission system calls.
     * 
     * @param permissions Array of permissions to validate
     * @return true if the request is safe to proceed, false if it should be blocked
     */
    public boolean validatePermissionRequest(String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            GrantlyLogger.w(TAG, "Invalid permission request: null or empty permissions array");
            return false;
        }
        
        // Check if system is in recovery mode
        if (systemInRecovery.get()) {
            long recoveryStartTime = lastSystemCallTime.get();
            if (System.currentTimeMillis() - recoveryStartTime < RECOVERY_TIMEOUT_MS) {
                GrantlyLogger.w(TAG, "System in recovery mode, blocking permission request");
                return false;
            } else {
                // Recovery timeout expired, reset recovery state
                systemInRecovery.set(false);
                GrantlyLogger.i(TAG, "System recovery completed");
            }
        }
        
        // Check request rate limiting
        if (!isRequestRateLimitOk()) {
            GrantlyLogger.w(TAG, "Request rate limit exceeded, blocking permission request");
            return false;
        }
        
        // Validate permission strings
        if (!validatePermissionStrings(permissions)) {
            GrantlyLogger.w(TAG, "Invalid permission strings detected, blocking request");
            return false;
        }
        
        // Ensure permissions are declared in our manifest
        if (!validatePermissionDeclarations(permissions)) {
            GrantlyLogger.w(TAG, "Undeclared permissions detected, blocking request");
            return false;
        }
        
        // Check concurrent request limits
        if (!checkConcurrentRequestLimits()) {
            GrantlyLogger.w(TAG, "Concurrent request limit exceeded, blocking permission request");
            return false;
        }
        
        GrantlyLogger.d(TAG, "Permission request validation passed for " + permissions.length + " permissions");
        return true;
    }
    
    /**
     * Records the start of a permission request for tracking purposes.
     * 
     * @param requestId Unique identifier for the request
     * @param permissions Array of permissions being requested
     */
    public void recordRequestStart(String requestId, String[] permissions) {
        if (requestId == null || permissions == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        globalRequestTracker.put(requestId, currentTime);
        lastSystemCallTime.set(currentTime);
        
        GrantlyLogger.d(TAG, "Recorded request start: " + requestId + " with " + permissions.length + " permissions");
        
        // Clean up old requests periodically
        cleanupOldRequests();
    }
    
    /**
     * Records the completion of a permission request.
     * 
     * @param requestId Unique identifier for the request
     */
    public void recordRequestComplete(String requestId) {
        if (requestId == null) {
            return;
        }
        
        Long startTime = globalRequestTracker.remove(requestId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            GrantlyLogger.d(TAG, "Request completed: " + requestId + " (duration: " + duration + "ms)");
        }
    }
    
    /**
     * Triggers system recovery mode if a serious error is detected.
     * This temporarily blocks all permission requests to prevent further issues.
     * 
     * @param reason Reason for entering recovery mode
     */
    public void triggerSystemRecovery(String reason) {
        systemInRecovery.set(true);
        lastSystemCallTime.set(System.currentTimeMillis());
        
        GrantlyLogger.w(TAG, "System recovery triggered: " + reason);
        
        // Clear all active requests to prevent further issues
        globalRequestTracker.clear();
    }
    
    /**
     * Checks if the current request rate is within acceptable limits.
     * 
     * @return true if rate limit is OK, false if exceeded
     */
    private boolean isRequestRateLimitOk() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - lastSystemCallTime.get();
        
        return timeSinceLastCall >= MIN_REQUEST_INTERVAL_MS;
    }
    
    /**
     * Validates that permission strings are well-formed and safe.
     * 
     * @param permissions Array of permission strings to validate
     * @return true if all permissions are valid, false otherwise
     */
    private boolean validatePermissionStrings(String[] permissions) {
        Set<String> uniquePermissions = new HashSet<>();
        
        for (String permission : permissions) {
            if (permission == null || permission.trim().isEmpty()) {
                GrantlyLogger.w(TAG, "Found null or empty permission string");
                return false;
            }
            
            // Check for malformed permission strings
            if (!isValidPermissionString(permission)) {
                GrantlyLogger.w(TAG, "Invalid permission string format: " + permission);
                return false;
            }
            
            // Check for duplicates
            if (!uniquePermissions.add(permission)) {
                GrantlyLogger.w(TAG, "Duplicate permission found: " + permission);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates that all permissions are properly declared in the app's manifest.
     * This is a critical security check to ensure we only request our own permissions.
     * 
     * @param permissions Array of permissions to validate
     * @return true if all permissions are declared, false otherwise
     */
    private boolean validatePermissionDeclarations(String[] permissions) {
        try {
            PackageManager pm = applicationContext.getPackageManager();
            android.content.pm.PackageInfo packageInfo = pm.getPackageInfo(
                packageName, PackageManager.GET_PERMISSIONS);
            
            if (packageInfo.requestedPermissions == null) {
                GrantlyLogger.w(TAG, "No permissions declared in manifest, but permissions requested");
                return false;
            }
            
            Set<String> declaredPermissions = new HashSet<>(Arrays.asList(packageInfo.requestedPermissions));
            
            for (String permission : permissions) {
                if (!declaredPermissions.contains(permission)) {
                    GrantlyLogger.w(TAG, "Permission not declared in manifest: " + permission);
                    return false;
                }
            }
            
            return true;
            
        } catch (PackageManager.NameNotFoundException e) {
            GrantlyLogger.e(TAG, "Unable to validate permissions - package not found", e);
            return false;
        }
    }
    
    /**
     * Checks if the current number of concurrent requests is within limits.
     * 
     * @return true if within limits, false if exceeded
     */
    private boolean checkConcurrentRequestLimits() {
        // Clean up old requests first
        cleanupOldRequests();
        
        return globalRequestTracker.size() < MAX_CONCURRENT_REQUESTS;
    }
    
    /**
     * Validates the format of a permission string.
     * 
     * @param permission Permission string to validate
     * @return true if valid format, false otherwise
     */
    private boolean isValidPermissionString(String permission) {
        // Basic format validation - should be like "android.permission.CAMERA"
        if (!permission.contains(".")) {
            return false;
        }
        
        // Should not contain suspicious characters that could cause system issues
        if (permission.contains("*") || permission.contains("?") || permission.contains("..")) {
            return false;
        }
        
        // Should not be excessively long (potential DoS)
        if (permission.length() > 256) {
            return false;
        }
        
        // Should follow standard Android permission naming convention
        String[] parts = permission.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Cleans up old requests that may have timed out.
     */
    private void cleanupOldRequests() {
        long currentTime = System.currentTimeMillis();
        
        globalRequestTracker.entrySet().removeIf(entry -> {
            long age = currentTime - entry.getValue();
            if (age > REQUEST_TIMEOUT_MS) {
                GrantlyLogger.d(TAG, "Cleaned up expired request: " + entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Performs a comprehensive system integrity check.
     * This method can be called periodically to ensure the permission system is healthy.
     * 
     * @return true if system integrity is OK, false if issues detected
     */
    public boolean performSystemIntegrityCheck() {
        GrantlyLogger.d(TAG, "Performing system integrity check");
        
        try {
            // Check if our package manager access is working correctly
            PackageManager pm = applicationContext.getPackageManager();
            android.content.pm.PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            
            if (packageInfo == null) {
                GrantlyLogger.e(TAG, "System integrity check failed: cannot access own package info");
                return false;
            }
            
            // Verify we're not accidentally affecting other packages
            if (!packageName.equals(packageInfo.packageName)) {
                GrantlyLogger.e(TAG, "System integrity check failed: package name mismatch");
                return false;
            }
            
            // Check if permission system is responding normally
            int cameraPermission = ContextCompat.checkSelfPermission(applicationContext, 
                android.Manifest.permission.CAMERA);
            
            // We don't care about the result, just that the call doesn't throw or hang
            GrantlyLogger.d(TAG, "System integrity check passed");
            return true;
            
        } catch (Exception e) {
            GrantlyLogger.e(TAG, "System integrity check failed with exception", e);
            return false;
        }
    }
    
    /**
     * Gets current system status for debugging purposes.
     * 
     * @return Status string with current system state
     */
    public String getSystemStatus() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCall = currentTime - lastSystemCallTime.get();
        
        return String.format(
            "Package: %s, Active Requests: %d, Recovery Mode: %b, Time Since Last Call: %dms",
            packageName,
            globalRequestTracker.size(),
            systemInRecovery.get(),
            timeSinceLastCall
        );
    }
}