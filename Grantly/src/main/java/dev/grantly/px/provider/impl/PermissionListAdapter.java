package dev.grantly.px.provider.impl;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.grantly.px.R;
import dev.grantly.px.util.GrantlyUtils;

/**
 * RecyclerView adapter for displaying permission items in the dialog.
 */
public class PermissionListAdapter extends RecyclerView.Adapter<PermissionListAdapter.PermissionViewHolder> {
    
    private final String[] permissions;
    
    public PermissionListAdapter(String[] permissions) {
        this.permissions = permissions != null ? permissions : new String[0];
    }
    
    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grantly_item_permission, parent, false);
        return new PermissionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        String permission = permissions[position];
        holder.bind(permission);
    }
    
    @Override
    public int getItemCount() {
        return permissions.length;
    }
    
    /**
     * ViewHolder for permission items.
     */
    static class PermissionViewHolder extends RecyclerView.ViewHolder {
        
        private final ImageView iconView;
        private final TextView nameView;
        private final TextView descriptionView;
        
        public PermissionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.grantly_permission_icon);
            nameView = itemView.findViewById(R.id.grantly_permission_name);
            descriptionView = itemView.findViewById(R.id.grantly_permission_description);
        }
        
        public void bind(String permission) {
            // Set permission icon
            if (iconView != null) {
                int iconRes = GrantlyUtils.getPermissionIconResource(permission);
                iconView.setImageResource(iconRes);
            }
            
            // Set permission name
            if (nameView != null) {
                String displayName = GrantlyUtils.getPermissionDisplayName(permission);
                nameView.setText(displayName);
            }
            
            // Set permission description
            if (descriptionView != null) {
                String description = GrantlyUtils.getPermissionDescription(permission);
                descriptionView.setText(description);
            }
        }
    }
}