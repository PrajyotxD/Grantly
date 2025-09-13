package dev.grantly.px.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.grantly.px.R;

/**
 * Utility class providing helper methods for permission management and system interactions.
 * Contains static methods for common permission-related operations.
 */
public class GrantlyUtils {
    
    private static final String TAG = "GrantlyUtils";
    
    // Special permissions that require specific handling
    private static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";
    private static final String MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";
    private static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    private static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
    
    // Common dangerous permissions
    private static final String CAMERA = "android.permission.CAMERA";
    private static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    private static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    private static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    private static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    private static final String CALL_PHONE = "android.permission.CALL_PHONE";
    private static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    private static final String SEND_SMS = "android.permission.SEND_SMS";
    private static final String READ_SMS = "android.permission.READ_SMS";
    private static final String READ_CALENDAR = "android.permission.READ_CALENDAR";
    private static final String WRITE_CALENDAR = "android.permission.WRITE_CALENDAR";
    
    // Permission display name mappings
    private static final Map<String, Integer> PERMISSION_NAMES = new HashMap<>();
    private static final Map<String, Integer> PERMISSION_DESCRIPTIONS = new HashMap<>();
    private static final Map<String, Integer> PERMISSION_ICONS = new HashMap<>();
    
    static {
        initializePermissionMappings();
    }
    
    // Prevent instantiation
    private GrantlyUtils() {
        throw new AssertionError("GrantlyUtils is a utility class and should not be instantiated");
    }
    
    /**
     * Check if a specific permission is declared in the app's AndroidManifest.xml
     * 
     * @param context Application context
     * @param permission Permission string to check
     * @return true if permission is declared, false otherwise
     */
    public static boolean isPermissionDeclared(Context context, String permission) {
        if (context == null || permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        Set<String> declaredPermissions = getDeclaredPermissionsInternal(context);
        return declaredPermissions.contains(permission);
    }
    
    /**
     * Open the app's settings page where users can manually grant permissions.
     * This is typically used when permissions are permanently denied.
     * 
     * @param context Context to start the settings activity
     */
    public static void openAppSettings(Context context) {
        if (context == null) {
            return;
        }
        
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // Fallback to general settings if app-specific settings fail
            try {
                Intent fallbackIntent = new Intent(Settings.ACTION_SETTINGS);
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallbackIntent);
            } catch (Exception fallbackException) {
                // Log error but don't crash the app
                android.util.Log.e(TAG, "Failed to open settings", fallbackException);
            }
        }
    }
    
    /**
     * Check if permissions are permanently denied (user selected "Don't ask again").
     * This method checks if shouldShowRequestPermissionRationale returns false for granted permissions.
     * 
     * @param activity Activity context needed for shouldShowRequestPermissionRationale
     * @param permissions Array of permissions to check
     * @return true if any of the permissions are permanently denied, false otherwise
     */
    public static boolean isPermanentlyDenied(Activity activity, String[] permissions) {
        if (activity == null || permissions == null || permissions.length == 0) {
            return false;
        }
        
        // Only check on API 23+ where runtime permissions exist
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        
        for (String permission : permissions) {
            if (permission == null || permission.trim().isEmpty()) {
                continue;
            }
            
            // Skip special permissions as they have different handling
            if (isSpecialPermission(permission)) {
                continue;
            }
            
            // Check if permission is denied and rationale should not be shown
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED &&
                !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get all dangerous permissions declared in the app's manifest.
     * Dangerous permissions are those that require runtime permission requests on API 23+.
     * 
     * @param context Application context
     * @return Array of dangerous permission strings, empty array if none found
     */
    public static String[] getDangerousPermissions(Context context) {
        if (context == null) {
            return new String[0];
        }
        
        Set<String> declaredPermissions = getDeclaredPermissionsInternal(context);
        Set<String> dangerousPermissions = new HashSet<>();
        
        PackageManager pm = context.getPackageManager();
        
        for (String permission : declaredPermissions) {
            if (isDangerousPermissionInternal(pm, permission)) {
                dangerousPermissions.add(permission);
            }
        }
        
        return dangerousPermissions.toArray(new String[0]);
    }
    
    /**
     * Check if a permission requires special handling (not standard runtime permission flow).
     * Special permissions include overlay permissions, settings permissions, etc.
     * 
     * @param permission Permission string to check
     * @return true if permission requires special handling, false otherwise
     */
    public static boolean isSpecialPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return false;
        }
        
        switch (permission) {
            case SYSTEM_ALERT_WINDOW:
            case WRITE_SETTINGS:
            case REQUEST_INSTALL_PACKAGES:
            case MANAGE_EXTERNAL_STORAGE:
                return true;
            case ACCESS_BACKGROUND_LOCATION:
                // Background location is special on Android 10+ (API 29+)
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
            case POST_NOTIFICATIONS:
                // Notification permission is special on Android 13+ (API 33+)
                return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
            default:
                return false;
        }
    }
    
    /**
     * Get the appropriate Intent for requesting a special permission.
     * Returns null if the permission is not a special permission or not supported on current API level.
     * 
     * @param permission Special permission string
     * @return Intent to request the special permission, or null if not applicable
     */
    public static Intent getSpecialPermissionIntent(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return null;
        }
        
        switch (permission) {
            case SYSTEM_ALERT_WINDOW:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                }
                break;
                
            case WRITE_SETTINGS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                }
                break;
                
            case REQUEST_INSTALL_PACKAGES:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                }
                break;
                
            case MANAGE_EXTERNAL_STORAGE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    return new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                }
                break;
                
            case ACCESS_BACKGROUND_LOCATION:
                // Background location uses standard permission flow but with special handling
                // Return null as it doesn't use a settings intent
                return null;
                
            case POST_NOTIFICATIONS:
                // Notification permission uses standard permission flow on API 33+
                // Return null as it doesn't use a settings intent
                return null;
                
            default:
                return null;
        }
        
        return null;
    }
    
    /**
     * Get user-friendly display name for a permission.
     * 
     * @param permission Permission string
     * @return Display name resource ID, or a default name if not found
     */
    public static String getPermissionDisplayName(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return "Unknown Permission";
        }
        
        Integer nameRes = PERMISSION_NAMES.get(permission);
        if (nameRes != null) {
            // In a real implementation, you would use context.getString(nameRes)
            // For now, return the permission name directly
            return getPermissionNameFromResource(nameRes);
        }
        
        // Fallback: extract name from permission string
        String[] parts = permission.split("\\.");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            return lastPart.replace("_", " ").toLowerCase();
        }
        
        return "Unknown Permission";
    }
    
    /**
     * Get user-friendly description for a permission.
     * 
     * @param permission Permission string
     * @return Description string
     */
    public static String getPermissionDescription(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return "This permission is required for the app to function properly.";
        }
        
        Integer descRes = PERMISSION_DESCRIPTIONS.get(permission);
        if (descRes != null) {
            return getPermissionDescriptionFromResource(descRes);
        }
        
        return "This permission is required for the app to function properly.";
    }
    
    /**
     * Get icon resource for a permission.
     * 
     * @param permission Permission string
     * @return Icon resource ID
     */
    public static int getPermissionIconResource(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            return R.drawable.grantly_ic_permission_item;
        }
        
        Integer iconRes = PERMISSION_ICONS.get(permission);
        if (iconRes != null) {
            return iconRes;
        }
        
        return R.drawable.grantly_ic_permission_item;
    }
    
    /**
     * Initialize permission mappings for display names, descriptions, and icons.
     */
    private static void initializePermissionMappings() {
        // Permission display names
        PERMISSION_NAMES.put(CAMERA, R.string.grantly_permission_camera);
        PERMISSION_NAMES.put(RECORD_AUDIO, R.string.grantly_permission_microphone);
        PERMISSION_NAMES.put(ACCESS_FINE_LOCATION, R.string.grantly_permission_location);
        PERMISSION_NAMES.put(ACCESS_COARSE_LOCATION, R.string.grantly_permission_location);
        PERMISSION_NAMES.put(ACCESS_BACKGROUND_LOCATION, R.string.grantly_permission_location);
        PERMISSION_NAMES.put(READ_EXTERNAL_STORAGE, R.string.grantly_permission_storage);
        PERMISSION_NAMES.put(WRITE_EXTERNAL_STORAGE, R.string.grantly_permission_storage);
        PERMISSION_NAMES.put(MANAGE_EXTERNAL_STORAGE, R.string.grantly_permission_storage);
        PERMISSION_NAMES.put(READ_CONTACTS, R.string.grantly_permission_contacts);
        PERMISSION_NAMES.put(WRITE_CONTACTS, R.string.grantly_permission_contacts);
        PERMISSION_NAMES.put(CALL_PHONE, R.string.grantly_permission_phone);
        PERMISSION_NAMES.put(READ_PHONE_STATE, R.string.grantly_permission_phone);
        PERMISSION_NAMES.put(SEND_SMS, R.string.grantly_permission_sms);
        PERMISSION_NAMES.put(READ_SMS, R.string.grantly_permission_sms);
        PERMISSION_NAMES.put(READ_CALENDAR, R.string.grantly_permission_calendar);
        PERMISSION_NAMES.put(WRITE_CALENDAR, R.string.grantly_permission_calendar);
        
        // Permission descriptions
        PERMISSION_DESCRIPTIONS.put(CAMERA, R.string.grantly_permission_camera_desc);
        PERMISSION_DESCRIPTIONS.put(RECORD_AUDIO, R.string.grantly_permission_microphone_desc);
        PERMISSION_DESCRIPTIONS.put(ACCESS_FINE_LOCATION, R.string.grantly_permission_location_desc);
        PERMISSION_DESCRIPTIONS.put(ACCESS_COARSE_LOCATION, R.string.grantly_permission_location_desc);
        PERMISSION_DESCRIPTIONS.put(ACCESS_BACKGROUND_LOCATION, R.string.grantly_permission_location_desc);
        PERMISSION_DESCRIPTIONS.put(READ_EXTERNAL_STORAGE, R.string.grantly_permission_storage_desc);
        PERMISSION_DESCRIPTIONS.put(WRITE_EXTERNAL_STORAGE, R.string.grantly_permission_storage_desc);
        PERMISSION_DESCRIPTIONS.put(MANAGE_EXTERNAL_STORAGE, R.string.grantly_permission_storage_desc);
        PERMISSION_DESCRIPTIONS.put(READ_CONTACTS, R.string.grantly_permission_contacts_desc);
        PERMISSION_DESCRIPTIONS.put(WRITE_CONTACTS, R.string.grantly_permission_contacts_desc);
        PERMISSION_DESCRIPTIONS.put(CALL_PHONE, R.string.grantly_permission_phone_desc);
        PERMISSION_DESCRIPTIONS.put(READ_PHONE_STATE, R.string.grantly_permission_phone_desc);
        PERMISSION_DESCRIPTIONS.put(SEND_SMS, R.string.grantly_permission_sms_desc);
        PERMISSION_DESCRIPTIONS.put(READ_SMS, R.string.grantly_permission_sms_desc);
        PERMISSION_DESCRIPTIONS.put(READ_CALENDAR, R.string.grantly_permission_calendar_desc);
        PERMISSION_DESCRIPTIONS.put(WRITE_CALENDAR, R.string.grantly_permission_calendar_desc);
        
        // Permission icons (using the same icon for now, can be customized later)
        PERMISSION_ICONS.put(CAMERA, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(RECORD_AUDIO, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(ACCESS_FINE_LOCATION, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(ACCESS_COARSE_LOCATION, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(ACCESS_BACKGROUND_LOCATION, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(READ_EXTERNAL_STORAGE, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(WRITE_EXTERNAL_STORAGE, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(MANAGE_EXTERNAL_STORAGE, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(READ_CONTACTS, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(WRITE_CONTACTS, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(CALL_PHONE, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(READ_PHONE_STATE, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(SEND_SMS, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(READ_SMS, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(READ_CALENDAR, R.drawable.grantly_ic_permission_item);
        PERMISSION_ICONS.put(WRITE_CALENDAR, R.drawable.grantly_ic_permission_item);
    }
    
    /**
     * Helper method to get permission name from resource ID.
     * In a real implementation, this would use Context.getString().
     */
    private static String getPermissionNameFromResource(int resourceId) {
        // This is a simplified implementation
        // In practice, you would use context.getString(resourceId)
        if (resourceId == R.string.grantly_permission_camera) return "Camera";
        if (resourceId == R.string.grantly_permission_microphone) return "Microphone";
        if (resourceId == R.string.grantly_permission_location) return "Location";
        if (resourceId == R.string.grantly_permission_storage) return "Storage";
        if (resourceId == R.string.grantly_permission_contacts) return "Contacts";
        if (resourceId == R.string.grantly_permission_phone) return "Phone";
        if (resourceId == R.string.grantly_permission_sms) return "SMS";
        if (resourceId == R.string.grantly_permission_calendar) return "Calendar";
        return "Permission";
    }
    
    /**
     * Helper method to get permission description from resource ID.
     * In a real implementation, this would use Context.getString().
     */
    private static String getPermissionDescriptionFromResource(int resourceId) {
        // This is a simplified implementation
        // In practice, you would use context.getString(resourceId)
        if (resourceId == R.string.grantly_permission_camera_desc) return "Access camera to take photos and videos";
        if (resourceId == R.string.grantly_permission_microphone_desc) return "Access microphone to record audio";
        if (resourceId == R.string.grantly_permission_location_desc) return "Access device location";
        if (resourceId == R.string.grantly_permission_storage_desc) return "Access device storage to read and write files";
        if (resourceId == R.string.grantly_permission_contacts_desc) return "Access contacts information";
        if (resourceId == R.string.grantly_permission_phone_desc) return "Access phone features and call information";
        if (resourceId == R.string.grantly_permission_sms_desc) return "Access SMS messages";
        if (resourceId == R.string.grantly_permission_calendar_desc) return "Access calendar events";
        return "This permission is required for the app to function properly.";
    }
    
    /**
     * Internal helper method to get all permissions declared in the app's AndroidManifest.xml
     * 
     * @param context Application context
     * @return Set of declared permission strings
     */
    private static Set<String> getDeclaredPermissionsInternal(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            
            Set<String> declaredPermissions = new HashSet<>();
            
            if (packageInfo.requestedPermissions != null) {
                declaredPermissions.addAll(Arrays.asList(packageInfo.requestedPermissions));
            }
            
            return declaredPermissions;
            
        } catch (PackageManager.NameNotFoundException e) {
            android.util.Log.e(TAG, "Unable to find package information", e);
            return new HashSet<>();
        }
    }
    
    /**
     * Internal helper method to check if a permission is considered dangerous
     * 
     * @param pm PackageManager instance
     * @param permission Permission string to check
     * @return true if permission is dangerous, false otherwise
     */
    private static boolean isDangerousPermissionInternal(PackageManager pm, String permission) {
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
            return false;
        }
    }
}