package dev.grantly.px.model;

/**
 * Represents the result of a permission check or request, containing the permission name,
 * its current state, and additional metadata about the permission.
 */
public class PermissionResult {
    private final String permission;
    private final PermissionState state;
    private final boolean requiresRationale;
    private final long timestamp;
    
    /**
     * Creates a new PermissionResult.
     *
     * @param permission The permission name (e.g., android.permission.CAMERA)
     * @param state The current state of the permission
     * @param requiresRationale Whether the permission requires showing rationale to the user
     */
    public PermissionResult(String permission, PermissionState state, boolean requiresRationale) {
        this.permission = permission;
        this.state = state;
        this.requiresRationale = requiresRationale;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the permission name.
     *
     * @return The permission name
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Gets the current state of the permission.
     *
     * @return The permission state
     */
    public PermissionState getState() {
        return state;
    }
    
    /**
     * Checks if the permission requires showing rationale to the user.
     *
     * @return true if rationale should be shown, false otherwise
     */
    public boolean requiresRationale() {
        return requiresRationale;
    }
    
    /**
     * Gets the timestamp when this result was created.
     *
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Checks if the permission is granted.
     *
     * @return true if the permission is granted, false otherwise
     */
    public boolean isGranted() {
        return state == PermissionState.GRANTED;
    }
    
    /**
     * Checks if the permission is denied (but not permanently).
     *
     * @return true if the permission is denied but can be requested again, false otherwise
     */
    public boolean isDenied() {
        return state == PermissionState.DENIED;
    }
    
    /**
     * Checks if the permission is permanently denied.
     *
     * @return true if the permission is permanently denied, false otherwise
     */
    public boolean isPermanentlyDenied() {
        return state == PermissionState.PERMANENTLY_DENIED;
    }
    
    /**
     * Checks if the permission requires special handling.
     *
     * @return true if the permission requires special handling, false otherwise
     */
    public boolean requiresSpecialHandling() {
        return state == PermissionState.REQUIRES_SPECIAL_HANDLING;
    }
    
    /**
     * Checks if the permission is not declared in the manifest.
     *
     * @return true if the permission is not declared, false otherwise
     */
    public boolean isNotDeclared() {
        return state == PermissionState.NOT_DECLARED;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PermissionResult that = (PermissionResult) obj;
        return requiresRationale == that.requiresRationale &&
               permission.equals(that.permission) &&
               state == that.state;
    }
    
    @Override
    public int hashCode() {
        int result = permission.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + (requiresRationale ? 1 : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "PermissionResult{" +
               "permission='" + permission + '\'' +
               ", state=" + state +
               ", requiresRationale=" + requiresRationale +
               ", timestamp=" + timestamp +
               '}';
    }
}