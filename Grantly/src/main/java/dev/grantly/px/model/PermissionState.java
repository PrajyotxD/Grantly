package dev.grantly.px.model;

/**
 * Represents the various states a permission can be in during the permission request lifecycle.
 * This enum covers all possible permission states across different Android versions and scenarios.
 */
public enum PermissionState {
    /**
     * Permission has been granted by the user.
     */
    GRANTED,
    
    /**
     * Permission has been denied by the user but can be requested again.
     */
    DENIED,
    
    /**
     * Permission has been permanently denied by the user (user selected "Don't ask again").
     * The app must redirect to settings for the user to grant the permission.
     */
    PERMANENTLY_DENIED,
    
    /**
     * Permission is not declared in the app's AndroidManifest.xml.
     * This is an error state that should be caught during development.
     */
    NOT_DECLARED,
    
    /**
     * Permission requires special handling (e.g., SYSTEM_ALERT_WINDOW, WRITE_SETTINGS).
     * These permissions cannot be requested through the standard runtime permission flow.
     */
    REQUIRES_SPECIAL_HANDLING
}