package dev.grantly.px.provider.impl;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.grantly.px.R;
import dev.grantly.px.provider.DialogProvider;
import dev.grantly.px.util.GrantlyUtils;

/**
 * Default implementation of DialogProvider that shows a standard Android dialog
 * with customizable layout and theming support.
 */
public class DefaultDialogProvider implements DialogProvider {
    
    private final int customLayoutRes;
    private final int themeRes;
    
    /**
     * Creates a DefaultDialogProvider with default layout and theme.
     */
    public DefaultDialogProvider() {
        this(R.layout.grantly_dialog_permission_request, 0);
    }
    
    /**
     * Creates a DefaultDialogProvider with custom layout.
     * 
     * @param customLayoutRes Custom layout resource ID, or 0 for default
     */
    public DefaultDialogProvider(int customLayoutRes) {
        this(customLayoutRes, 0);
    }
    
    /**
     * Creates a DefaultDialogProvider with custom layout and theme.
     * 
     * @param customLayoutRes Custom layout resource ID, or 0 for default
     * @param themeRes Theme resource ID, or 0 for default
     */
    public DefaultDialogProvider(int customLayoutRes, int themeRes) {
        this.customLayoutRes = customLayoutRes != 0 ? customLayoutRes : R.layout.grantly_dialog_permission_request;
        this.themeRes = themeRes;
    }
    
    @Override
    public void showPermissionDialog(Context context, String[] permissions, DialogCallback callback) {
        if (context == null || permissions == null || permissions.length == 0 || callback == null) {
            if (callback != null) {
                callback.onDialogResult(false);
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
            
            setupDialogViews(dialogView, permissions, callback, dialog);
            
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(dialogInterface -> callback.onDialogResult(false));
            
            dialog.show();
            
        } catch (Exception e) {
            // Fallback to simple dialog if custom layout fails
            showFallbackDialog(context, permissions, callback);
        }
    }
    
    /**
     * Sets up the views in the dialog layout.
     */
    private void setupDialogViews(View dialogView, String[] permissions, DialogCallback callback, AlertDialog dialog) {
        // Setup icon
        ImageView iconView = dialogView.findViewById(R.id.grantly_request_icon);
        if (iconView != null) {
            iconView.setImageResource(R.drawable.grantly_ic_permission_request);
        }
        
        // Setup title
        TextView titleView = dialogView.findViewById(R.id.grantly_request_title);
        if (titleView != null) {
            titleView.setText(R.string.grantly_request_default_title);
        }
        
        // Setup message
        TextView messageView = dialogView.findViewById(R.id.grantly_request_message);
        if (messageView != null) {
            String message = generatePermissionMessage(permissions);
            messageView.setText(message);
        }
        
        // Setup permission list
        RecyclerView permissionList = dialogView.findViewById(R.id.grantly_permission_list);
        if (permissionList != null) {
            setupPermissionList(permissionList, permissions);
        }
        
        // Setup buttons
        Button allowButton = dialogView.findViewById(R.id.grantly_request_allow);
        Button denyButton = dialogView.findViewById(R.id.grantly_request_deny);
        
        if (allowButton != null) {
            allowButton.setOnClickListener(v -> {
                dialog.dismiss();
                callback.onDialogResult(true);
            });
        }
        
        if (denyButton != null) {
            denyButton.setOnClickListener(v -> {
                dialog.dismiss();
                callback.onDialogResult(false);
            });
        }
    }
    
    /**
     * Sets up the RecyclerView for displaying permissions.
     */
    private void setupPermissionList(RecyclerView recyclerView, String[] permissions) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        PermissionListAdapter adapter = new PermissionListAdapter(permissions);
        recyclerView.setAdapter(adapter);
    }
    
    /**
     * Generates a user-friendly message for the requested permissions.
     */
    private String generatePermissionMessage(String[] permissions) {
        Context context = null; // Will be passed from the calling method
        if (permissions.length == 1) {
            return "This app needs " + GrantlyUtils.getPermissionDisplayName(permissions[0]) + " permission to function properly.";
        } else {
            return "This app needs the following permissions to function properly:";
        }
    }
    
    /**
     * Shows a fallback dialog when the custom layout fails to load.
     */
    private void showFallbackDialog(Context context, String[] permissions, DialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        String title = context.getString(R.string.grantly_request_default_title);
        String message = generatePermissionMessage(permissions);
        
        builder.setTitle(title)
               .setMessage(message)
               .setPositiveButton(R.string.grantly_allow, (dialog, which) -> {
                   dialog.dismiss();
                   callback.onDialogResult(true);
               })
               .setNegativeButton(R.string.grantly_deny, (dialog, which) -> {
                   dialog.dismiss();
                   callback.onDialogResult(false);
               })
               .setCancelable(true)
               .setOnCancelListener(dialog -> callback.onDialogResult(false));
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}