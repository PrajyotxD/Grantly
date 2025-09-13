package dev.grantly.px.exception;

/**
 * Exception thrown when attempting to start a new permission request while
 * another permission request is already in progress.
 * 
 * This exception helps prevent concurrent permission requests which can lead
 * to undefined behavior and poor user experience.
 */
public class PermissionRequestInProgressException extends GrantlyException {
    
    private final String activeRequestId;
    private final String[] activePermissions;
    private final long requestStartTime;
    
    /**
     * Constructs a new PermissionRequestInProgressException with active request details.
     *
     * @param activeRequestId unique identifier of the active permission request
     */
    public PermissionRequestInProgressException(String activeRequestId) {
        this(activeRequestId, null, System.currentTimeMillis());
    }
    
    /**
     * Constructs a new PermissionRequestInProgressException with active request details.
     *
     * @param activeRequestId unique identifier of the active permission request
     * @param activePermissions array of permissions being requested in the active request
     */
    public PermissionRequestInProgressException(String activeRequestId, String[] activePermissions) {
        this(activeRequestId, activePermissions, System.currentTimeMillis());
    }
    
    /**
     * Constructs a new PermissionRequestInProgressException with full active request details.
     *
     * @param activeRequestId unique identifier of the active permission request
     * @param activePermissions array of permissions being requested in the active request
     * @param requestStartTime timestamp when the active request was started
     */
    public PermissionRequestInProgressException(String activeRequestId, String[] activePermissions, long requestStartTime) {
        super(buildErrorMessage(activeRequestId, activePermissions));
        this.activeRequestId = activeRequestId;
        this.activePermissions = activePermissions != null ? activePermissions.clone() : null;
        this.requestStartTime = requestStartTime;
    }
    
    /**
     * Returns the unique identifier of the active permission request.
     *
     * @return the active request ID
     */
    public String getActiveRequestId() {
        return activeRequestId;
    }
    
    /**
     * Returns the permissions being requested in the active request.
     *
     * @return array of active permissions, or null if not available
     */
    public String[] getActivePermissions() {
        return activePermissions != null ? activePermissions.clone() : null;
    }
    
    /**
     * Returns the timestamp when the active request was started.
     *
     * @return request start time in milliseconds since epoch
     */
    public long getRequestStartTime() {
        return requestStartTime;
    }
    
    /**
     * Returns the duration for which the active request has been running.
     *
     * @return duration in milliseconds
     */
    public long getRequestDuration() {
        return System.currentTimeMillis() - requestStartTime;
    }
    
    /**
     * Returns a formatted string of the active permissions being requested.
     *
     * @return comma-separated list of active permissions, or "Unknown" if not available
     */
    public String getActivePermissionsString() {
        if (activePermissions == null || activePermissions.length == 0) {
            return "Unknown";
        }
        return String.join(", ", activePermissions);
    }
    
    /**
     * Returns resolution guidance for handling the concurrent request issue.
     *
     * @return multi-line string with resolution instructions
     */
    public String getResolutionGuidance() {
        StringBuilder guidance = new StringBuilder();
        guidance.append("To resolve this issue:\n");
        guidance.append("1. Wait for the current permission request to complete before starting a new one\n");
        guidance.append("2. Implement proper request lifecycle management in your app\n");
        guidance.append("3. Consider using request queuing if multiple permission requests are needed\n");
        guidance.append("4. Check if the active request (ID: ").append(activeRequestId).append(") can be cancelled\n");
        
        long duration = getRequestDuration();
        if (duration > 30000) { // 30 seconds
            guidance.append("\nNote: The active request has been running for ")
                    .append(duration / 1000)
                    .append(" seconds, which may indicate a stuck request.");
        }
        
        return guidance.toString();
    }
    
    private static String buildErrorMessage(String activeRequestId, String[] activePermissions) {
        StringBuilder message = new StringBuilder();
        message.append("Cannot start new permission request - another request is already in progress");
        
        if (activeRequestId != null) {
            message.append(" (Request ID: ").append(activeRequestId).append(")");
        }
        
        if (activePermissions != null && activePermissions.length > 0) {
            message.append("\nActive request permissions: ").append(String.join(", ", activePermissions));
        }
        
        message.append("\n\nOnly one permission request can be active at a time. ")
               .append("Wait for the current request to complete before starting a new one.");
        
        return message.toString();
    }
    
    // Static factory methods for common scenarios
    
    /**
     * Creates a PermissionRequestInProgressException for a generic active request.
     *
     * @return configured exception for generic active request
     */
    public static PermissionRequestInProgressException generic() {
        return new PermissionRequestInProgressException("unknown");
    }
    
    /**
     * Creates a PermissionRequestInProgressException for a system dialog active request.
     *
     * @param permissions the permissions being requested in the system dialog
     * @return configured exception for system dialog request
     */
    public static PermissionRequestInProgressException systemDialog(String[] permissions) {
        return new PermissionRequestInProgressException("system_dialog", permissions);
    }
    
    /**
     * Creates a PermissionRequestInProgressException for a rationale dialog active request.
     *
     * @param permissions the permissions being requested in the rationale dialog
     * @return configured exception for rationale dialog request
     */
    public static PermissionRequestInProgressException rationaleDialog(String[] permissions) {
        return new PermissionRequestInProgressException("rationale_dialog", permissions);
    }
}