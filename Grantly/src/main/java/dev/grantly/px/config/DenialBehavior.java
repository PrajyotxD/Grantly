package dev.grantly.px.config;

/**
 * Enum defining different behaviors when permissions are denied by the user.
 * This allows developers to configure how the SDK should respond to permission denials.
 */
public enum DenialBehavior {
    /**
     * Continue normal app flow even when permissions are denied.
     * The app should handle the lack of permissions gracefully.
     */
    CONTINUE_APP_FLOW,
    
    /**
     * Disable specific features that require the denied permissions.
     * The app continues to function but with limited capabilities.
     */
    DISABLE_FEATURE,
    
    /**
     * Show a dialog explaining why the app cannot function without the permissions,
     * then exit the app if the user chooses not to grant them.
     */
    EXIT_APP_WITH_DIALOG,
    
    /**
     * Immediately exit the app when critical permissions are denied.
     * Use this for permissions that are absolutely essential for app functionality.
     */
    EXIT_APP_IMMEDIATELY
}