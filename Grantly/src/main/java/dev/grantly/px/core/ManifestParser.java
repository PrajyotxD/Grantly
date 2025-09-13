package dev.grantly.px.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;

import dev.grantly.px.exception.PermissionNotDeclaredException;
import dev.grantly.px.util.GrantlyLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for parsing AndroidManifest.xml and extracting permission information.
 * Handles categorization of dangerous vs normal permissions and validation of declared permissions.
 */
public class ManifestParser {
    
    /**
     * Creates a new ManifestParser instance.
     */
    public ManifestParser() {
        // Default constructor
    }
    
    private static final String TAG = "ManifestParser";
    
    // Cache for declared permissions to avoid repeated parsing
    private Set<String> cachedDeclaredPermissions;
    private Set<String> cachedDangerousPermissions;
    private String cachedPackageName;
    
    /**
     * Get all permissions declared in the app's AndroidManifest.xml
     * 
     * @param context Application context
     * @return Set of declared permission strings
     * @throws SecurityException if unable to access package information
     */
    Set<String> getDeclaredPermissions(Context context) {
        String packageName = context.getPackageName();
        
        GrantlyLogger.d(TAG, "Parsing declared permissions for package: " + packageName);
        
        // Return cached result if available for same package
        if (cachedDeclaredPermissions != null && packageName.equals(cachedPackageName)) {
            GrantlyLogger.d(TAG, "Returning cached declared permissions (" + cachedDeclaredPermissions.size() + " permissions)");
            return new HashSet<>(cachedDeclaredPermissions);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            
            Set<String> declaredPermissions = new HashSet<>();
            
            if (packageInfo.requestedPermissions != null) {
                declaredPermissions.addAll(Arrays.asList(packageInfo.requestedPermissions));
                GrantlyLogger.d(TAG, "Found " + packageInfo.requestedPermissions.length + " declared permissions");
            } else {
                GrantlyLogger.w(TAG, "No permissions declared in manifest");
            }
            
            // Cache the result
            cachedDeclaredPermissions = new HashSet<>(declaredPermissions);
            cachedPackageName = packageName;
            
            long duration = System.currentTimeMillis() - startTime;
            GrantlyLogger.logPerformance(TAG, "getDeclaredPermissions", duration);
            
            return declaredPermissions;
            
        } catch (PackageManager.NameNotFoundException e) {
            GrantlyLogger.e(TAG, "Unable to find package information for: " + packageName, e);
            throw new SecurityException("Unable to find package information for: " + packageName, e);
        }
    }
    
    /**
     * Get only the dangerous permissions declared in the app's manifest.
     * Dangerous permissions are those that require runtime permission requests on API 23+.
     * 
     * @param context Application context
     * @return Set of dangerous permission strings declared in manifest
     */
    Set<String> getDangerousPermissions(Context context) {
        String packageName = context.getPackageName();
        
        GrantlyLogger.d(TAG, "Getting dangerous permissions for package: " + packageName);
        
        // Return cached result if available for same package
        if (cachedDangerousPermissions != null && packageName.equals(cachedPackageName)) {
            GrantlyLogger.d(TAG, "Returning cached dangerous permissions (" + cachedDangerousPermissions.size() + " permissions)");
            return new HashSet<>(cachedDangerousPermissions);
        }
        
        Set<String> declaredPermissions = getDeclaredPermissions(context);
        Set<String> dangerousPermissions = new HashSet<>();
        
        PackageManager pm = context.getPackageManager();
        
        for (String permission : declaredPermissions) {
            if (isDangerousPermission(pm, permission)) {
                dangerousPermissions.add(permission);
                GrantlyLogger.v(TAG, "Identified dangerous permission: " + permission);
            }
        }
        
        // Cache the result
        cachedDangerousPermissions = new HashSet<>(dangerousPermissions);
        
        GrantlyLogger.i(TAG, "Found " + dangerousPermissions.size() + " dangerous permissions out of " + declaredPermissions.size() + " total declared permissions");
        
        return dangerousPermissions;
    }
    
    /**
     * Check if a specific permission is declared in the app's manifest
     * 
     * @param context Application context
     * @param permission Permission string to check
     * @return true if permission is declared, false otherwise
     */
    boolean isPermissionDeclared(Context context, String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        Set<String> declaredPermissions = getDeclaredPermissions(context);
        return declaredPermissions.contains(permission);
    }
    
    /**
     * Validate that all requested permissions are declared in the manifest.
     * Throws PermissionNotDeclaredException if any permission is not declared.
     * 
     * @param context Application context
     * @param requestedPermissions Array of permissions to validate
     * @throws PermissionNotDeclaredException if any permission is not declared
     */
    void validatePermissions(Context context, String[] requestedPermissions) {
        if (requestedPermissions == null || requestedPermissions.length == 0) {
            return;
        }
        
        Set<String> declaredPermissions = getDeclaredPermissions(context);
        Set<String> undeclaredPermissions = new HashSet<>();
        
        for (String permission : requestedPermissions) {
            if (permission != null && !permission.trim().isEmpty() && 
                !declaredPermissions.contains(permission)) {
                undeclaredPermissions.add(permission);
            }
        }
        
        if (!undeclaredPermissions.isEmpty()) {
            throw new PermissionNotDeclaredException(
                undeclaredPermissions.toArray(new String[0])
            );
        }
    }
    
    /**
     * Check if a permission is considered dangerous (requires runtime permission on API 23+)
     * 
     * @param pm PackageManager instance
     * @param permission Permission string to check
     * @return true if permission is dangerous, false otherwise
     */
    private boolean isDangerousPermission(PackageManager pm, String permission) {
        try {
            PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
            
            // On API 23+, check the protection level
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int protectionLevel = permissionInfo.getProtection();
                return protectionLevel == PermissionInfo.PROTECTION_DANGEROUS;
            } else {
                // For older versions, use the deprecated protectionLevel field
                @SuppressWarnings("deprecation")
                int protectionLevel = permissionInfo.protectionLevel;
                return (protectionLevel & PermissionInfo.PROTECTION_MASK_BASE) == 
                       PermissionInfo.PROTECTION_DANGEROUS;
            }
            
        } catch (PackageManager.NameNotFoundException e) {
            // If permission info not found, assume it's not dangerous
            // This handles custom permissions or system permissions not in the current API
            return false;
        }
    }
    
    /**
     * Clear cached permission data. Useful for testing or when package info might change.
     */
    void clearCache() {
        cachedDeclaredPermissions = null;
        cachedDangerousPermissions = null;
        cachedPackageName = null;
    }
}