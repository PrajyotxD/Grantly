package dev.grantly.px.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when attempting to request permissions that are not declared
 * in the application's AndroidManifest.xml file.
 * 
 * This exception provides detailed information about which permissions are missing
 * and guidance on how to resolve the issue.
 */
public class PermissionNotDeclaredException extends GrantlyException {
    
    private final List<String> undeclaredPermissions;
    private final String manifestPath;
    
    /**
     * Constructs a new PermissionNotDeclaredException for a single undeclared permission.
     *
     * @param permission the permission that was not declared in the manifest
     */
    public PermissionNotDeclaredException(String permission) {
        this(new String[]{permission});
    }
    
    /**
     * Constructs a new PermissionNotDeclaredException for multiple undeclared permissions.
     *
     * @param undeclaredPermissions array of permissions that were not declared in the manifest
     */
    public PermissionNotDeclaredException(String[] undeclaredPermissions) {
        super(buildErrorMessage(undeclaredPermissions));
        this.undeclaredPermissions = Collections.unmodifiableList(Arrays.asList(undeclaredPermissions));
        this.manifestPath = "src/main/AndroidManifest.xml";
    }
    
    /**
     * Constructs a new PermissionNotDeclaredException with custom message and undeclared permissions.
     *
     * @param message custom error message
     * @param undeclaredPermissions array of permissions that were not declared in the manifest
     */
    public PermissionNotDeclaredException(String message, String[] undeclaredPermissions) {
        super(message);
        this.undeclaredPermissions = Collections.unmodifiableList(Arrays.asList(undeclaredPermissions));
        this.manifestPath = "src/main/AndroidManifest.xml";
    }
    
    /**
     * Returns the list of permissions that were not declared in the manifest.
     *
     * @return unmodifiable list of undeclared permissions
     */
    public List<String> getUndeclaredPermissions() {
        return undeclaredPermissions;
    }
    
    /**
     * Returns the expected path to the AndroidManifest.xml file.
     *
     * @return the manifest file path
     */
    public String getManifestPath() {
        return manifestPath;
    }
    
    /**
     * Returns a formatted string of all undeclared permissions.
     *
     * @return comma-separated list of undeclared permissions
     */
    public String getUndeclaredPermissionsString() {
        return String.join(", ", undeclaredPermissions);
    }
    
    /**
     * Returns detailed resolution guidance for fixing the missing permissions.
     *
     * @return multi-line string with step-by-step resolution instructions
     */
    public String getResolutionGuidance() {
        StringBuilder guidance = new StringBuilder();
        guidance.append("To resolve this issue:\n");
        guidance.append("1. Open your AndroidManifest.xml file (typically located at ").append(manifestPath).append(")\n");
        guidance.append("2. Add the following permission declarations inside the <manifest> tag:\n\n");
        
        for (String permission : undeclaredPermissions) {
            guidance.append("   <uses-permission android:name=\"").append(permission).append("\" />\n");
        }
        
        guidance.append("\n3. Sync your project and rebuild\n");
        guidance.append("4. Ensure the permissions are appropriate for your app's functionality");
        
        return guidance.toString();
    }
    
    private static String buildErrorMessage(String[] undeclaredPermissions) {
        if (undeclaredPermissions == null || undeclaredPermissions.length == 0) {
            return "Attempted to request permissions that are not declared in AndroidManifest.xml";
        }
        
        StringBuilder message = new StringBuilder();
        if (undeclaredPermissions.length == 1) {
            message.append("Permission '").append(undeclaredPermissions[0]).append("' is not declared in AndroidManifest.xml");
        } else {
            message.append("The following permissions are not declared in AndroidManifest.xml: ");
            message.append(String.join(", ", undeclaredPermissions));
        }
        
        message.append("\n\nAll permissions must be declared in your AndroidManifest.xml before they can be requested at runtime.");
        return message.toString();
    }
}