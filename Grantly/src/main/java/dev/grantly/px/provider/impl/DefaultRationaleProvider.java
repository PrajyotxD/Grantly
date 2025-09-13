package dev.grantly.px.provider.impl;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import dev.grantly.px.R;
import dev.grantly.px.provider.RationaleProvider;
import dev.grantly.px.util.GrantlyUtils;

/**
 * Default implementation of RationaleProvider that shows a standard rationale dialog
 * with customizable layout and theming support.
 */
public class DefaultRationaleProvider implements RationaleProvider {
    
    private final int customLayoutRes;
    private final int themeRes;
    private final String customTitle;
    private final String customMessage;
    
    /**
     * Creates a DefaultRationaleProvider with default layout and theme.
     */
    public DefaultRationaleProvider() {
        this(R.layout.grantly_dialog_rationale, 0, null, null);
    }
    
    /**
     * Creates a DefaultRationaleProvider with custom title and message.
     * 
     * @param customTitle Custom title for the rationale dialog
     * @param customMessage Custom message for the rationale dialog
     */
    public DefaultRationaleProvider(String customTitle, String customMessage) {
        this(R.layout.grantly_dialog_rationale, 0, customTitle, customMessage);
    }
    
    /**
     * Creates a DefaultRationaleProvider with custom layout.
     * 
     * @param customLayoutRes Custom layout resource ID, or 0 for default
     */
    public DefaultRationaleProvider(int customLayoutRes) {
        this(customLayoutRes, 0, null, null);
    }
    
    /**
     * Creates a DefaultRationaleProvider with all customization options.
     * 
     * @param customLayoutRes Custom layout resource ID, or 0 for default
     * @param themeRes Theme resource ID, or 0 for default
     * @param customTitle Custom title, or null for default
     * @param customMessage Custom message, or null for default
     */
    public DefaultRationaleProvider(int customLayoutRes, int themeRes, String customTitle, String customMessage) {
        this.customLayoutRes = customLayoutRes != 0 ? customLayoutRes : R.layout.grantly_dialog_rationale;
        this.themeRes = themeRes;
        this.customTitle = customTitle;
        this.customMessage = customMessage;
    }
    
    @Override
    public void showRationale(Context context, String[] permissions, RationaleCallback callback) {
        if (context == null || permissions == null || permissions.length == 0 || callback == null) {
            if (callback != null) {
                callback.onRationaleDenied();
            }
            return;
        }
        
        try {
            AlertDialog.Builder builder = themeRes != 0 
                ? new AlertDialog.Builder(context, themeRes)
                : new AlertDialog.Builder(context, R.style.GrantlyTheme);
            
            View dialogView = LayoutInflater.from(context).inflate(customLayoutRes, null);
            builder.setView(dialogView);
            
            AlertDialog dialog = builder.create();
            
            setupRationaleViews(dialogView, permissions, callback, dialog, context);
            
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(dialogInterface -> callback.onRationaleDenied());
            
            dialog.show();
            
        } catch (Exception e) {
            // Fallback to simple dialog if custom layout fails
            showFallbackRationaleDialog(context, permissions, callback);
        }
    }
    
    /**
     * Sets up the views in the rationale dialog layout.
     */
    private void setupRationaleViews(View dialogView, String[] permissions, RationaleCallback callback, 
                                   AlertDialog dialog, Context context) {
        // Setup icon
        ImageView iconView = dialogView.findViewById(R.id.grantly_rationale_icon);
        if (iconView != null) {
            iconView.setImageResource(R.drawable.grantly_ic_permission);
        }
        
        // Setup title
        TextView titleView = dialogView.findViewById(R.id.grantly_rationale_title);
        if (titleView != null) {
            String title = customTitle != null ? customTitle : context.getString(R.string.grantly_rationale_default_title);
            titleView.setText(title);
        }
        
        // Setup message
        TextView messageView = dialogView.findViewById(R.id.grantly_rationale_message);
        if (messageView != null) {
            String message = customMessage != null ? customMessage : generateRationaleMessage(permissions, context);
            messageView.setText(message);
        }
        
        // Setup buttons
        Button continueButton = dialogView.findViewById(R.id.grantly_rationale_continue);
        Button cancelButton = dialogView.findViewById(R.id.grantly_rationale_cancel);
        
        if (continueButton != null) {
            continueButton.setOnClickListener(v -> {
                dialog.dismiss();
                callback.onRationaleAccepted();
            });
        }
        
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> {
                dialog.dismiss();
                callback.onRationaleDenied();
            });
        }
    }
    
    /**
     * Generates a user-friendly rationale message for the requested permissions.
     */
    private String generateRationaleMessage(String[] permissions, Context context) {
        if (permissions.length == 1) {
            String permissionName = GrantlyUtils.getPermissionDisplayName(permissions[0]);
            return "This app needs " + permissionName + " permission to provide you with the best experience. " +
                   "Please allow this permission to continue.";
        } else {
            StringBuilder message = new StringBuilder("This app needs the following permissions to function properly:\n\n");
            for (String permission : permissions) {
                String permissionName = GrantlyUtils.getPermissionDisplayName(permission);
                String description = GrantlyUtils.getPermissionDescription(permission);
                message.append("• ").append(permissionName).append(": ").append(description).append("\n");
            }
            message.append("\nPlease allow these permissions to continue.");
            return message.toString();
        }
    }
    
    /**
     * Shows a fallback rationale dialog when the custom layout fails to load.
     */
    private void showFallbackRationaleDialog(Context context, String[] permissions, RationaleCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        String title = customTitle != null ? customTitle : context.getString(R.string.grantly_rationale_default_title);
        String message = customMessage != null ? customMessage : generateRationaleMessage(permissions, context);
        
        builder.setTitle(title)
               .setMessage(message)
               .setPositiveButton(R.string.grantly_continue, (dialog, which) -> {
                   dialog.dismiss();
                   callback.onRationaleAccepted();
               })
               .setNegativeButton(R.string.grantly_cancel, (dialog, which) -> {
                   dialog.dismiss();
                   callback.onRationaleDenied();
               })
               .setCancelable(true)
               .setOnCancelListener(dialog -> callback.onRationaleDenied());
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}