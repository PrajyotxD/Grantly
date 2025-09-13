package dev.grantly.px.callback;

import java.util.List;
import dev.grantly.px.model.PermissionResult;

/**
 * Callback interface for handling permission request results.
 * 
 * <p>This interface provides methods to handle different permission states after a request
 * is completed. Implementations should handle all possible outcomes to provide a good
 * user experience regardless of the permission decision.</p>
 * 
 * <p><strong>Permission States:</strong></p>
 * <ul>
 *   <li><strong>Granted:</strong> User approved the permission - feature can be used</li>
 *   <li><strong>Denied:</strong> User denied the permission but can be asked again</li>
 *   <li><strong>Permanently Denied:</strong> User denied with "Don't ask again" - need to go to settings</li>
 *   <li><strong>Cancelled:</strong> User dismissed the permission dialog without choosing</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong> All callback methods are called on the main/UI thread,
 * so it's safe to update UI elements directly from these methods.</p>
 * 
 * @since 1.0.0
 * @see PermissionResult
 * @see dev.grantly.px.util.GrantlyUtils#openAppSettings(android.content.Context)
 */
public interface GrantlyCallback {
    
    /**
     * Called when all requested permissions are granted.
     * 
     * <p>This method is invoked when the user has granted all the permissions that were
     * requested. The app can now proceed with the functionality that required these permissions.</p>
     * 
     * @param permissions Array of permission strings that were granted
     * @since 1.0.0
     */
    void onPermissionGranted(String[] permissions);
    
    /**
     * Called when one or more permissions are denied but not permanently.
     * 
     * <p>The user has denied the permissions but has not selected "Don't ask again".
     * The app can request these permissions again in the future, preferably with
     * additional rationale explaining why they are needed.</p>
     * 
     * @param permissions Array of permission strings that were denied
     * @since 1.0.0
     */
    void onPermissionDenied(String[] permissions);
    
    /**
     * Called when one or more permissions are permanently denied.
     * 
     * <p>The user has either checked "Don't ask again" or denied the permission multiple times.
     * The only way to grant these permissions now is through the app settings. Consider
     * using {@link dev.grantly.px.util.GrantlyUtils#openAppSettings} to guide the user.</p>
     * 
     * @param permissions Array of permission strings that were permanently denied
     * @since 1.0.0
     */
    void onPermissionPermanentlyDenied(String[] permissions);
    
    /**
     * Called when the permission request is cancelled by the user.
     * This is optional and has a default empty implementation.
     */
    default void onPermissionRequestCancelled() {
        // Default empty implementation
    }
    
    /**
     * Called with the detailed results of the permission request.
     * This method provides comprehensive information about each permission's state.
     * 
     * @param results List of PermissionResult objects containing detailed information
     *                about each permission that was requested
     */
    void onPermissionResult(List<PermissionResult> results);
}