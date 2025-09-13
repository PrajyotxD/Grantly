package dev.grantly.px.provider;

import android.content.Context;

/**
 * Interface for providing custom dialog UI for permission requests.
 * Implement this interface to show custom permission request dialogs.
 */
public interface DialogProvider {
    
    /**
     * Show a custom dialog for permission requests.
     * This allows complete customization of the permission request UI.
     * 
     * @param context The context to use for showing the dialog
     * @param permissions Array of permissions being requested
     * @param callback Callback to notify the result of the dialog interaction
     */
    void showPermissionDialog(Context context, String[] permissions, DialogCallback callback);
    
    /**
     * Callback interface for dialog provider results.
     */
    interface DialogCallback {
        
        /**
         * Called when the dialog interaction results in permission grant/deny.
         * 
         * @param granted true if user granted permissions, false if denied
         */
        void onDialogResult(boolean granted);
    }
}