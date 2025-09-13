package dev.grantly.px.provider;

import android.content.Context;

/**
 * Interface for providing custom toast messages for permission results.
 * Implement this interface to show custom toast notifications.
 */
public interface ToastProvider {
    
    /**
     * Show a toast message for permission granted.
     * 
     * @param context The context to use for showing the toast
     * @param permissions Array of permissions that were granted
     */
    void showPermissionGrantedToast(Context context, String[] permissions);
    
    /**
     * Show a toast message for permission denied.
     * 
     * @param context The context to use for showing the toast
     * @param permissions Array of permissions that were denied
     */
    void showPermissionDeniedToast(Context context, String[] permissions);
    
    /**
     * Show a toast message for permission permanently denied.
     * 
     * @param context The context to use for showing the toast
     * @param permissions Array of permissions that were permanently denied
     */
    void showPermissionPermanentlyDeniedToast(Context context, String[] permissions);
    
    /**
     * Show a custom toast message.
     * 
     * @param context The context to use for showing the toast
     * @param message The message to display
     * @param duration Toast duration (Toast.LENGTH_SHORT or Toast.LENGTH_LONG)
     */
    void showCustomToast(Context context, String message, int duration);
}