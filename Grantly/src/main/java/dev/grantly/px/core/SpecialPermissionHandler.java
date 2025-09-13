package dev.grantly.px.core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import dev.grantly.px.util.GrantlyUtils;

/**
 * Handles special permissions that require custom flows beyond standard runtime permissions.
 * Special permissions include overlay permissions, settings permissions, and other system-level permissions
 * that need to be requested through Settings intents rather than the standard permission dialog.
 */
public class SpecialPermissionHandler {
    
    private static final String TAG = "SpecialPermissionHandler";
    
    // Special permission constants
    private static final String SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final String REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";
    private static final String MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";
    private static final String ACCESS_BACKGROUND_LOCATION = "android.permission.ACCESS_BACKGROUND_LOCATION";
    private static final String POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";
    
    // Request codes for special permissions
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1002;
    private static final int REQUEST_CODE_INSTALL_PACKAGES = 1003;
    private static final int REQUEST_CODE_MANAGE_STORAGE = 1004;
    private static final int REQUEST_CODE_BACKGROUND_LOCATION = 1005;
    
    /**
     * Callback interface for special permission results
     */
    public interface SpecialPermissionCallback {
        /**
         * Called when a special permission request is completed
         * @param permission The permission that was requested
         * @param granted Whether the permission was granted
         */
        void onSpecialPermissionResult(String permission, boolean granted);
    }
    
    private SpecialPermissionCallback callback;
    
    /**
     * Creates a new SpecialPermissionHandler without a callback
     */
    public SpecialPermissionHandler() {
        this.callback = null;
    }
    
    /**
     * Creates a new SpecialPermissionHandler with the specified callback
     * @param callback Callback to receive permission results
     */
    public SpecialPermissionHandler(SpecialPermissionCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Sets the callback for receiving permission results
     * @param callback Callback to receive permission results
     */
    public void setCallback(SpecialPermissionCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Check if a permission requires special handling
     * @param permission Permission to check
     * @return true if permission requires special handling
     */
    public boolean isSpecialPermission(String permission) {
        return GrantlyUtils.isSpecialPermission(permission);
    }
    
    /**
     * Handle a special permission request by launching the appropriate Settings intent
     * @param activity Activity context for launching intents
     * @param permission Special permission to request
     * @return true if the special permission flow was initiated, false if not applicable
     */
    public boolean handleSpecialPermission(Activity activity, String permission) {
        if (activity == null || permission == null) {
            return false;
        }
        
        switch (permission) {
            case SYSTEM_ALERT_WINDOW:
                return handleOverlayPermission(activity);
                
            case WRITE_SETTINGS:
                return handleWriteSettingsPermission(activity);
                
            case REQUEST_INSTALL_PACKAGES:
                return handleInstallPackagesPermission(activity);
                
            case MANAGE_EXTERNAL_STORAGE:
                return handleManageExternalStoragePermission(activity);
                
            case ACCESS_BACKGROUND_LOCATION:
                return handleBackgroundLocationPermission(activity);
                
            case POST_NOTIFICATIONS:
                return handleNotificationPermission(activity);
                
            default:
                // Permission is not handled by this method
                return false;
        }
    }
    
    /**
     * Handle SYSTEM_ALERT_WINDOW permission request
     * @param activity Activity context
     * @return true if intent was launched successfully
     */
    private boolean handleOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                
                // CRITICAL SECURITY FIX: Always specify our own package to prevent affecting other apps
                String ourPackage = activity.getPackageName();
                Uri uri = Uri.parse("package:" + ourPackage);
                intent.setData(uri);
                
                android.util.Log.d(TAG, "Launching overlay permission for package: " + ourPackage);
                
                activity.startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
                return true;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to launch overlay permission settings", e);
                return false;
            }
        } else {
            // On pre-M devices, overlay permission is granted at install time
            if (callback != null) {
                callback.onSpecialPermissionResult(SYSTEM_ALERT_WINDOW, true);
            }
            return true;
        }
    }
    
    /**
     * Handle WRITE_SETTINGS permission request
     * @param activity Activity context
     * @return true if intent was launched successfully
     */
    private boolean handleWriteSettingsPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                
                // CRITICAL SECURITY FIX: Always specify our own package to prevent affecting other apps
                String ourPackage = activity.getPackageName();
                Uri uri = Uri.parse("package:" + ourPackage);
                intent.setData(uri);
                
                android.util.Log.d(TAG, "Launching write settings permission for package: " + ourPackage);
                
                activity.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
                return true;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to launch write settings permission", e);
                return false;
            }
        } else {
            // On pre-M devices, write settings permission is granted at install time
            if (callback != null) {
                callback.onSpecialPermissionResult(WRITE_SETTINGS, true);
            }
            return true;
        }
    }
    
    /**
     * Handle REQUEST_INSTALL_PACKAGES permission request
     * @param activity Activity context
     * @return true if intent was launched successfully
     */
    private boolean handleInstallPackagesPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                
                // CRITICAL SECURITY FIX: Always specify our own package to prevent affecting other apps
                String ourPackage = activity.getPackageName();
                Uri uri = Uri.parse("package:" + ourPackage);
                intent.setData(uri);
                
                android.util.Log.d(TAG, "Launching install packages permission for package: " + ourPackage);
                
                activity.startActivityForResult(intent, REQUEST_CODE_INSTALL_PACKAGES);
                return true;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to launch install packages permission", e);
                return false;
            }
        } else {
            // On pre-O devices, install packages permission is granted at install time
            if (callback != null) {
                callback.onSpecialPermissionResult(REQUEST_INSTALL_PACKAGES, true);
            }
            return true;
        }
    }
    
    /**
     * Handle MANAGE_EXTERNAL_STORAGE permission request
     * @param activity Activity context
     * @return true if intent was launched successfully
     */
    private boolean handleManageExternalStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                
                // CRITICAL SECURITY FIX: Always specify our own package to prevent affecting other apps
                String ourPackage = activity.getPackageName();
                Uri uri = Uri.parse("package:" + ourPackage);
                intent.setData(uri);
                
                android.util.Log.d(TAG, "Launching manage external storage permission for package: " + ourPackage);
                
                activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE);
                return true;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Failed to launch manage external storage permission", e);
                return false;
            }
        } else {
            // On pre-R devices, use legacy storage permissions
            if (callback != null) {
                callback.onSpecialPermissionResult(MANAGE_EXTERNAL_STORAGE, false);
            }
            return true;
        }
    }
    
    /**
     * Handle ACCESS_BACKGROUND_LOCATION permission request
     * This requires a two-step process: first request fine/coarse location, then background location
     * @param activity Activity context
     * @return true if the background location flow was initiated
     */
    private boolean handleBackgroundLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // SECURITY FIX: Add safety checks to prevent system interference
            try {
                // Check if fine/coarse location permissions are granted first
                boolean hasFineLocation = ActivityCompat.checkSelfPermission(activity, 
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean hasCoarseLocation = ActivityCompat.checkSelfPermission(activity, 
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                
                // SAFETY: Only proceed if we have foreground location permission first
                if (!hasFineLocation && !hasCoarseLocation) {
                    // Log the safety check
                    android.util.Log.d(TAG, "Background location requires foreground location first - requesting foreground");
                    
                    // CRITICAL: Only request for our own app's package
                    String ourPackage = activity.getPackageName();
                    android.util.Log.d(TAG, "Requesting foreground location for package: " + ourPackage);
                    
                    ActivityCompat.requestPermissions(activity, 
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 
                        REQUEST_CODE_BACKGROUND_LOCATION);
                    return true;
                } else {
                    // Fine/coarse location already granted, request background location
                    android.util.Log.d(TAG, "Foreground location granted, requesting background location");
                    
                    // CRITICAL: Only request for our own app's package
                    String ourPackage = activity.getPackageName();
                    android.util.Log.d(TAG, "Requesting background location for package: " + ourPackage);
                    
                    ActivityCompat.requestPermissions(activity, 
                        new String[]{ACCESS_BACKGROUND_LOCATION}, 
                        REQUEST_CODE_BACKGROUND_LOCATION);
                    return true;
                }
            } catch (SecurityException e) {
                android.util.Log.e(TAG, "Security exception during background location request", e);
                if (callback != null) {
                    callback.onSpecialPermissionResult(ACCESS_BACKGROUND_LOCATION, false);
                }
                return false;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Unexpected exception during background location request", e);
                if (callback != null) {
                    callback.onSpecialPermissionResult(ACCESS_BACKGROUND_LOCATION, false);
                }
                return false;
            }
        } else {
            // On pre-Q devices, background location is granted with fine/coarse location
            if (callback != null) {
                callback.onSpecialPermissionResult(ACCESS_BACKGROUND_LOCATION, true);
            }
            return true;
        }
    }
    
    /**
     * Handle POST_NOTIFICATIONS permission request (Android 13+)
     * @param activity Activity context
     * @return true if the notification permission flow was initiated
     */
    private boolean handleNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                // CRITICAL: Only request for our own app's package
                String ourPackage = activity.getPackageName();
                android.util.Log.d(TAG, "Requesting notification permission for package: " + ourPackage);
                
                // Use standard permission request for notification permission
                ActivityCompat.requestPermissions(activity, 
                    new String[]{POST_NOTIFICATIONS}, 
                    REQUEST_CODE_BACKGROUND_LOCATION); // Reuse request code as it's handled similarly
                return true;
            } catch (SecurityException e) {
                android.util.Log.e(TAG, "Security exception during notification permission request", e);
                if (callback != null) {
                    callback.onSpecialPermissionResult(POST_NOTIFICATIONS, false);
                }
                return false;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Unexpected exception during notification permission request", e);
                if (callback != null) {
                    callback.onSpecialPermissionResult(POST_NOTIFICATIONS, false);
                }
                return false;
            }
        } else {
            // On pre-Tiramisu devices, notification permission is granted at install time
            if (callback != null) {
                callback.onSpecialPermissionResult(POST_NOTIFICATIONS, true);
            }
            return true;
        }
    }
    
    /**
     * Check if a special permission is currently granted
     * @param context Context for checking permission status
     * @param permission Special permission to check
     * @return true if permission is granted
     */
    public boolean isSpecialPermissionGranted(Context context, String permission) {
        if (context == null || permission == null) {
            return false;
        }
        
        switch (permission) {
            case SYSTEM_ALERT_WINDOW:
                return isOverlayPermissionGranted(context);
                
            case WRITE_SETTINGS:
                return isWriteSettingsPermissionGranted(context);
                
            case REQUEST_INSTALL_PACKAGES:
                return isInstallPackagesPermissionGranted(context);
                
            case MANAGE_EXTERNAL_STORAGE:
                return isManageExternalStoragePermissionGranted(context);
                
            case ACCESS_BACKGROUND_LOCATION:
                return isBackgroundLocationPermissionGranted(context);
                
            case POST_NOTIFICATIONS:
                return isNotificationPermissionGranted(context);
                
            default:
                return false;
        }
    }
    
    /**
     * Check if overlay permission is granted
     * @param context Context for checking permission
     * @return true if overlay permission is granted
     */
    private boolean isOverlayPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            // On pre-M devices, overlay permission is granted at install time if declared
            return GrantlyUtils.isPermissionDeclared(context, SYSTEM_ALERT_WINDOW);
        }
    }
    
    /**
     * Check if write settings permission is granted
     * @param context Context for checking permission
     * @return true if write settings permission is granted
     */
    private boolean isWriteSettingsPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(context);
        } else {
            // On pre-M devices, write settings permission is granted at install time if declared
            return GrantlyUtils.isPermissionDeclared(context, WRITE_SETTINGS);
        }
    }
    
    /**
     * Check if install packages permission is granted
     * @param context Context for checking permission
     * @return true if install packages permission is granted
     */
    private boolean isInstallPackagesPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getPackageManager().canRequestPackageInstalls();
        } else {
            // On pre-O devices, install packages permission is granted at install time if declared
            return GrantlyUtils.isPermissionDeclared(context, REQUEST_INSTALL_PACKAGES);
        }
    }
    
    /**
     * Check if manage external storage permission is granted
     * @param context Context for checking permission
     * @return true if manage external storage permission is granted
     */
    private boolean isManageExternalStoragePermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            // On pre-R devices, use legacy storage permissions
            return ActivityCompat.checkSelfPermission(context, 
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Check if background location permission is granted
     * @param context Context for checking permission
     * @return true if background location permission is granted
     */
    private boolean isBackgroundLocationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
        } else {
            // On pre-Q devices, background location is granted with fine/coarse location
            return ActivityCompat.checkSelfPermission(context, 
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, 
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    /**
     * Check if notification permission is granted
     * @param context Context for checking permission
     * @return true if notification permission is granted
     */
    private boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED;
        } else {
            // On pre-Tiramisu devices, notification permission is granted at install time
            return true;
        }
    }
    
    /**
     * Get the appropriate Intent for requesting a special permission
     * @param permission Special permission to get intent for
     * @return Intent for requesting the permission, or null if not applicable
     */
    public Intent getSpecialPermissionIntent(String permission) {
        return GrantlyUtils.getSpecialPermissionIntent(permission);
    }
    
    /**
     * Handle activity result from special permission requests
     * @param activity Activity that received the result
     * @param requestCode Request code from the result
     * @param resultCode Result code from the result
     * @param data Intent data from the result
     * @return true if the result was handled by this handler
     */
    public boolean handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (activity == null || callback == null) {
            return false;
        }
        
        switch (requestCode) {
            case REQUEST_CODE_OVERLAY_PERMISSION:
                boolean overlayGranted = isOverlayPermissionGranted(activity);
                callback.onSpecialPermissionResult(SYSTEM_ALERT_WINDOW, overlayGranted);
                return true;
                
            case REQUEST_CODE_WRITE_SETTINGS:
                boolean writeSettingsGranted = isWriteSettingsPermissionGranted(activity);
                callback.onSpecialPermissionResult(WRITE_SETTINGS, writeSettingsGranted);
                return true;
                
            case REQUEST_CODE_INSTALL_PACKAGES:
                boolean installPackagesGranted = isInstallPackagesPermissionGranted(activity);
                callback.onSpecialPermissionResult(REQUEST_INSTALL_PACKAGES, installPackagesGranted);
                return true;
                
            case REQUEST_CODE_MANAGE_STORAGE:
                boolean manageStorageGranted = isManageExternalStoragePermissionGranted(activity);
                callback.onSpecialPermissionResult(MANAGE_EXTERNAL_STORAGE, manageStorageGranted);
                return true;
                
            case REQUEST_CODE_BACKGROUND_LOCATION:
                // This handles both background location and notification permissions
                // Check which permission was actually requested based on current state
                boolean backgroundLocationGranted = isBackgroundLocationPermissionGranted(activity);
                boolean notificationGranted = isNotificationPermissionGranted(activity);
                
                // Determine which permission was being requested
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
                    GrantlyUtils.isPermissionDeclared(activity, ACCESS_BACKGROUND_LOCATION)) {
                    callback.onSpecialPermissionResult(ACCESS_BACKGROUND_LOCATION, backgroundLocationGranted);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
                    GrantlyUtils.isPermissionDeclared(activity, POST_NOTIFICATIONS)) {
                    callback.onSpecialPermissionResult(POST_NOTIFICATIONS, notificationGranted);
                }
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handle standard permission results for special permissions that use the standard flow
     * @param requestCode Request code from the permission result
     * @param permissions Permissions that were requested
     * @param grantResults Grant results for the permissions
     * @return true if the result was handled by this handler
     */
    public boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (callback == null || permissions == null || grantResults == null) {
            return false;
        }
        
        if (requestCode == REQUEST_CODE_BACKGROUND_LOCATION) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                
                if (ACCESS_BACKGROUND_LOCATION.equals(permission) || 
                    POST_NOTIFICATIONS.equals(permission) ||
                    android.Manifest.permission.ACCESS_FINE_LOCATION.equals(permission) ||
                    android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                    
                    callback.onSpecialPermissionResult(permission, granted);
                }
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Get request code for a special permission
     * @param permission Special permission
     * @return Request code for the permission, or -1 if not applicable
     */
    public int getRequestCodeForPermission(String permission) {
        if (permission == null) {
            return -1;
        }
        
        switch (permission) {
            case SYSTEM_ALERT_WINDOW:
                return REQUEST_CODE_OVERLAY_PERMISSION;
            case WRITE_SETTINGS:
                return REQUEST_CODE_WRITE_SETTINGS;
            case REQUEST_INSTALL_PACKAGES:
                return REQUEST_CODE_INSTALL_PACKAGES;
            case MANAGE_EXTERNAL_STORAGE:
                return REQUEST_CODE_MANAGE_STORAGE;
            case ACCESS_BACKGROUND_LOCATION:
            case POST_NOTIFICATIONS:
                return REQUEST_CODE_BACKGROUND_LOCATION;
            default:
                return -1;
        }
    }
}