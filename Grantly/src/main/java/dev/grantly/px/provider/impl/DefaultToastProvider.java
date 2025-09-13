package dev.grantly.px.provider.impl;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dev.grantly.px.R;
import dev.grantly.px.provider.ToastProvider;
import dev.grantly.px.util.GrantlyUtils;

/**
 * Default implementation of ToastProvider that shows styleable toast messages
 * with custom layouts and theming support.
 */
public class DefaultToastProvider implements ToastProvider {
    
    private final int customLayoutRes;
    private final boolean useCustomLayout;
    
    /**
     * Creates a DefaultToastProvider with default styling.
     */
    public DefaultToastProvider() {
        this(0);
    }
    
    /**
     * Creates a DefaultToastProvider with custom layout.
     * 
     * @param customLayoutRes Custom layout resource ID, or 0 for default system toast
     */
    public DefaultToastProvider(int customLayoutRes) {
        this.customLayoutRes = customLayoutRes;
        this.useCustomLayout = customLayoutRes != 0;
    }
    
    @Override
    public void showPermissionGrantedToast(Context context, String[] permissions) {
        if (context == null || permissions == null || permissions.length == 0) {
            return;
        }
        
        String message = generateGrantedMessage(permissions);
        showToastInternal(context, message, Toast.LENGTH_SHORT, R.drawable.grantly_ic_permission, true);
    }
    
    @Override
    public void showPermissionDeniedToast(Context context, String[] permissions) {
        if (context == null || permissions == null || permissions.length == 0) {
            return;
        }
        
        String message = generateDeniedMessage(permissions);
        showToastInternal(context, message, Toast.LENGTH_SHORT, R.drawable.grantly_ic_permission, false);
    }
    
    @Override
    public void showPermissionPermanentlyDeniedToast(Context context, String[] permissions) {
        if (context == null || permissions == null || permissions.length == 0) {
            return;
        }
        
        String message = generatePermanentlyDeniedMessage(permissions);
        showToastInternal(context, message, Toast.LENGTH_LONG, R.drawable.grantly_ic_permission, false);
    }
    
    @Override
    public void showCustomToast(Context context, String message, int duration) {
        if (context == null || message == null || message.trim().isEmpty()) {
            return;
        }
        
        showToastInternal(context, message, duration, R.drawable.grantly_ic_permission, true);
    }
    
    /**
     * Internal method to show toast with custom styling.
     */
    private void showToastInternal(Context context, String message, int duration, int iconRes, boolean isSuccess) {
        if (useCustomLayout) {
            showCustomStyledToast(context, message, duration, iconRes, isSuccess);
        } else {
            showSystemToast(context, message, duration);
        }
    }
    
    /**
     * Shows a custom styled toast using the provided layout.
     */
    private void showCustomStyledToast(Context context, String message, int duration, int iconRes, boolean isSuccess) {
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(customLayoutRes, null);
            
            // Setup icon if present
            ImageView iconView = layout.findViewById(R.id.grantly_toast_icon);
            if (iconView != null) {
                iconView.setImageResource(iconRes);
                // You could change icon color based on success/failure
                if (isSuccess) {
                    iconView.setColorFilter(context.getResources().getColor(R.color.grantly_success));
                } else {
                    iconView.setColorFilter(context.getResources().getColor(R.color.grantly_error));
                }
            }
            
            // Setup message
            TextView messageView = layout.findViewById(R.id.grantly_toast_message);
            if (messageView != null) {
                messageView.setText(message);
            }
            
            Toast toast = new Toast(context);
            toast.setDuration(duration);
            toast.setView(layout);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
            toast.show();
            
        } catch (Exception e) {
            // Fallback to system toast if custom layout fails
            showSystemToast(context, message, duration);
        }
    }
    
    /**
     * Shows a standard system toast.
     */
    private void showSystemToast(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
    
    /**
     * Generates a message for granted permissions.
     */
    private String generateGrantedMessage(String[] permissions) {
        if (permissions.length == 1) {
            String permissionName = GrantlyUtils.getPermissionDisplayName(permissions[0]);
            return permissionName + " permission granted";
        } else {
            return "Permissions granted";
        }
    }
    
    /**
     * Generates a message for denied permissions.
     */
    private String generateDeniedMessage(String[] permissions) {
        if (permissions.length == 1) {
            String permissionName = GrantlyUtils.getPermissionDisplayName(permissions[0]);
            return permissionName + " permission denied";
        } else {
            return "Permissions denied";
        }
    }
    
    /**
     * Generates a message for permanently denied permissions.
     */
    private String generatePermanentlyDeniedMessage(String[] permissions) {
        if (permissions.length == 1) {
            String permissionName = GrantlyUtils.getPermissionDisplayName(permissions[0]);
            return permissionName + " permission permanently denied. Please enable it in Settings.";
        } else {
            return "Permissions permanently denied. Please enable them in Settings.";
        }
    }
}