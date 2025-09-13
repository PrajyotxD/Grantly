package dev.grantly.px.provider;

import android.content.Context;

/**
 * Interface for providing custom rationale UI when permissions need explanation.
 * Implement this interface to show custom rationale dialogs or UI components.
 */
public interface RationaleProvider {
    
    /**
     * Show rationale UI to explain why the permissions are needed.
     * This is called when Android's shouldShowRequestPermissionRationale returns true.
     * 
     * @param context The context to use for showing the rationale UI
     * @param permissions Array of permissions that need rationale explanation
     * @param callback Callback to notify when user accepts or denies the rationale
     */
    void showRationale(Context context, String[] permissions, RationaleCallback callback);
    
    /**
     * Callback interface for rationale provider results.
     */
    interface RationaleCallback {
        
        /**
         * Called when the user accepts the rationale and wants to proceed with permission request.
         */
        void onRationaleAccepted();
        
        /**
         * Called when the user denies the rationale and doesn't want to grant permissions.
         */
        void onRationaleDenied();
    }
}