package dev.grantly.px.core;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import dev.grantly.px.model.PermissionResult;
import dev.grantly.px.model.PermissionState;
import dev.grantly.px.util.GrantlyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles permission state checking across different Android versions.
 * This class provides version-specific permission checking logic with proper fallbacks
 * for different Android API levels.
 */
public class PermissionChecker {
    
    private final Context context;
    private final ManifestParser manifestParser;
    
    /**
     * Creates a new PermissionChecker instance.
     *
     * @param context The application context
     */
    public PermissionChecker(Context context) {
        this.context = context.getApplicationContext();
        this.manifestParser = new ManifestParser();
    }
    
    /**
     * Checks the current state of a single permission.
     *
     * @param permission The permission to check
     * @return PermissionResult containing the permission state and metadata
     */
    public PermissionResult checkPermission(String permission) {
        // First check if permission is declared in manifest
        if (!manifestParser.isPermissionDeclared(context, permission)) {
            return new PermissionResult(permission, PermissionState.NOT_DECLARED, false);
        }
        
        // Check if it's a special permission that requires different handling
        if (GrantlyUtils.isSpecialPermission(permission)) {
            boolean isGranted = checkSpecialPermission(permission);
            PermissionState state = isGranted ? PermissionState.GRANTED : PermissionState.REQUIRES_SPECIAL_HANDLING;
            return new PermissionResult(permission, state, false);
        }
        
        // For Android 6.0+ (API 23+), use runtime permission system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkRuntimePermission(permission);
        } else {
            // For pre-Android 6.0, permissions are granted at install time
            return checkInstallTimePermission(permission);
        }
    }
    
    /**
     * Checks the state of multiple permissions.
     *
     * @param permissions Array of permissions to check
     * @return List of PermissionResult objects
     */
    public List<PermissionResult> checkPermissions(String[] permissions) {
        List<PermissionResult> results = new ArrayList<>();
        for (String permission : permissions) {
            results.add(checkPermission(permission));
        }
        return results;
    }
    
    /**
     * Checks if any of the given permissions are granted.
     *
     * @param permissions Array of permissions to check
     * @return true if at least one permission is granted, false otherwise
     */
    public boolean hasAnyPermission(String[] permissions) {
        for (String permission : permissions) {
            PermissionResult result = checkPermission(permission);
            if (result.isGranted()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if all of the given permissions are granted.
     *
     * @param permissions Array of permissions to check
     * @return true if all permissions are granted, false otherwise
     */
    public boolean hasAllPermissions(String[] permissions) {
        for (String permission : permissions) {
            PermissionResult result = checkPermission(permission);
            if (!result.isGranted()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if the app should show rationale for requesting a permission.
     * This method handles version-specific behavior correctly.
     *
     * @param activity The activity context
     * @param permission The permission to check
     * @return true if rationale should be shown, false otherwise
     */
    public boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        }
        // Pre-Android 6.0 doesn't have runtime permissions, so no rationale needed
        return false;
    }
    
    /**
     * Determines if a permission is permanently denied.
     * A permission is considered permanently denied if:
     * 1. It's not granted
     * 2. shouldShowRequestPermissionRationale returns false
     * 3. The permission has been requested before
     *
     * @param activity The activity context
     * @param permission The permission to check
     * @return true if the permission is permanently denied, false otherwise
     */
    public boolean isPermanentlyDenied(Activity activity, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Pre-Android 6.0 doesn't have runtime permissions
            return false;
        }
        
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        
        // If we should show rationale, it's not permanently denied
        if (shouldShowRequestPermissionRationale(activity, permission)) {
            return false;
        }
        
        // At this point, permission is denied and no rationale should be shown
        // This could mean either:
        // 1. First time asking (not permanently denied)
        // 2. User selected "Don't ask again" (permanently denied)
        // We need additional context to determine this, which should be tracked
        // by the calling code. For now, we assume it's permanently denied
        // if rationale is not shown and permission is not granted.
        return true;
    }
    
    /**
     * Gets all permissions that are currently denied.
     *
     * @param permissions Array of permissions to check
     * @return Array of denied permissions
     */
    public String[] getDeniedPermissions(String[] permissions) {
        List<String> denied = new ArrayList<>();
        for (String permission : permissions) {
            PermissionResult result = checkPermission(permission);
            if (result.isDenied() || result.isPermanentlyDenied()) {
                denied.add(permission);
            }
        }
        return denied.toArray(new String[0]);
    }
    
    /**
     * Gets all permissions that are currently granted.
     *
     * @param permissions Array of permissions to check
     * @return Array of granted permissions
     */
    public String[] getGrantedPermissions(String[] permissions) {
        List<String> granted = new ArrayList<>();
        for (String permission : permissions) {
            PermissionResult result = checkPermission(permission);
            if (result.isGranted()) {
                granted.add(permission);
            }
        }
        return granted.toArray(new String[0]);
    }
    
    /**
     * Checks runtime permission state for Android 6.0+ devices.
     *
     * @param permission The permission to check
     * @return PermissionResult with current state
     */
    private PermissionResult checkRuntimePermission(String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return new PermissionResult(permission, PermissionState.GRANTED, false);
        } else {
            // Permission is denied, but we need activity context to determine if rationale should be shown
            // For now, we'll return DENIED and let the caller determine rationale
            return new PermissionResult(permission, PermissionState.DENIED, false);
        }
    }
    
    /**
     * Checks install-time permission state for pre-Android 6.0 devices.
     *
     * @param permission The permission to check
     * @return PermissionResult with current state
     */
    private PermissionResult checkInstallTimePermission(String permission) {
        // For pre-Android 6.0, if permission is declared in manifest, it's granted
        int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
        
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return new PermissionResult(permission, PermissionState.GRANTED, false);
        } else {
            // This shouldn't happen if permission is properly declared, but handle gracefully
            return new PermissionResult(permission, PermissionState.DENIED, false);
        }
    }
    
    /**
     * Checks special permissions that require different handling.
     *
     * @param permission The special permission to check
     * @return true if the special permission is granted, false otherwise
     */
    private boolean checkSpecialPermission(String permission) {
        switch (permission) {
            case android.Manifest.permission.SYSTEM_ALERT_WINDOW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return android.provider.Settings.canDrawOverlays(context);
                }
                return true; // Pre-M, this permission is granted at install time
                
            case android.Manifest.permission.WRITE_SETTINGS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return android.provider.Settings.System.canWrite(context);
                }
                return true; // Pre-M, this permission is granted at install time
                
            case android.Manifest.permission.MANAGE_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    return android.os.Environment.isExternalStorageManager();
                }
                // For older versions, fall back to regular storage permissions
                return ContextCompat.checkSelfPermission(context, 
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                
            case android.Manifest.permission.REQUEST_INSTALL_PACKAGES:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return context.getPackageManager().canRequestPackageInstalls();
                }
                return true; // Pre-O, this permission is granted at install time
                
            default:
                // For other permissions, use standard permission check
                return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
    }
}