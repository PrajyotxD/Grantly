package dev.grantly.px.core;

import android.app.Activity;
import dev.grantly.px.callback.GrantlyCallback;

import java.util.Arrays;
import java.util.UUID;

/**
 * Manages the lifecycle and context of a permission request.
 * This class holds all the necessary information for tracking and managing
 * an ongoing permission request throughout its lifecycle.
 */
public class RequestContext {
    private final String requestId;
    private final Activity activity;
    private final String[] permissions;
    private final GrantlyCallback callback;
    private final long timestamp;
    private final boolean isLazy;
    private final Object originalRequest;
    
    private volatile boolean isActive;
    private volatile boolean isCompleted;
    
    /**
     * Creates a new RequestContext for managing a permission request.
     *
     * @param activity The activity context for the request
     * @param permissions Array of permissions being requested
     * @param callback The callback to invoke when the request completes
     * @param isLazy Whether this is a lazy permission request
     * @param originalRequest The original PermissionRequest object (stored as Object to avoid circular dependency)
     */
    public RequestContext(Activity activity, String[] permissions, GrantlyCallback callback, 
                         boolean isLazy, Object originalRequest) {
        this.requestId = UUID.randomUUID().toString();
        this.activity = activity;
        this.permissions = permissions.clone(); // Defensive copy
        this.callback = callback;
        this.isLazy = isLazy;
        this.originalRequest = originalRequest;
        this.timestamp = System.currentTimeMillis();
        this.isActive = true;
        this.isCompleted = false;
    }
    
    /**
     * Gets the unique request ID.
     *
     * @return The request ID
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Gets the activity context for this request.
     *
     * @return The activity context
     */
    public Activity getActivity() {
        return activity;
    }
    
    /**
     * Gets the permissions being requested.
     *
     * @return Array of permission names
     */
    public String[] getPermissions() {
        return permissions.clone(); // Defensive copy
    }
    
    /**
     * Gets the callback for this request.
     *
     * @return The callback interface
     */
    public GrantlyCallback getCallback() {
        return callback;
    }
    
    /**
     * Gets the timestamp when this request was created.
     *
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Checks if this is a lazy permission request.
     *
     * @return true if lazy, false otherwise
     */
    public boolean isLazy() {
        return isLazy;
    }
    
    /**
     * Gets the original request object.
     *
     * @return The original PermissionRequest object
     */
    public Object getOriginalRequest() {
        return originalRequest;
    }
    
    /**
     * Checks if the request is currently active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive && !isCompleted;
    }
    
    /**
     * Checks if the request has been completed.
     *
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return isCompleted;
    }
    
    /**
     * Marks the request as inactive (cancelled or paused).
     */
    public void setInactive() {
        this.isActive = false;
    }
    
    /**
     * Marks the request as active.
     */
    public void setActive() {
        if (!isCompleted) {
            this.isActive = true;
        }
    }
    
    /**
     * Marks the request as completed.
     * Once completed, the request cannot be reactivated.
     */
    public void setCompleted() {
        this.isActive = false;
        this.isCompleted = true;
    }
    
    /**
     * Checks if the activity is still valid (not finishing or destroyed).
     *
     * @return true if the activity is valid, false otherwise
     */
    public boolean isActivityValid() {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }
    
    /**
     * Gets the age of this request in milliseconds.
     *
     * @return The age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Checks if a specific permission is included in this request.
     *
     * @param permission The permission to check
     * @return true if the permission is included, false otherwise
     */
    public boolean containsPermission(String permission) {
        for (String p : permissions) {
            if (p.equals(permission)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RequestContext that = (RequestContext) obj;
        return requestId.equals(that.requestId);
    }
    
    @Override
    public int hashCode() {
        return requestId.hashCode();
    }
    
    @Override
    public String toString() {
        return "RequestContext{" +
               "requestId='" + requestId + '\'' +
               ", permissions=" + Arrays.toString(permissions) +
               ", isLazy=" + isLazy +
               ", isActive=" + isActive +
               ", isCompleted=" + isCompleted +
               ", timestamp=" + timestamp +
               '}';
    }
}