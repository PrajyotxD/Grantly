package dev.grantly.px.exception;

/**
 * Exception thrown when the Grantly SDK is configured with invalid parameters
 * or when configuration validation fails.
 * 
 * This exception provides specific information about what configuration
 * issue was encountered and how to resolve it.
 */
public class InvalidConfigurationException extends GrantlyException {
    
    private final String configurationIssue;
    private final String suggestedFix;
    
    /**
     * Constructs a new InvalidConfigurationException with a configuration issue description.
     *
     * @param configurationIssue description of the configuration problem
     */
    public InvalidConfigurationException(String configurationIssue) {
        super(buildErrorMessage(configurationIssue, null));
        this.configurationIssue = configurationIssue;
        this.suggestedFix = null;
    }
    
    /**
     * Constructs a new InvalidConfigurationException with issue description and suggested fix.
     *
     * @param configurationIssue description of the configuration problem
     * @param suggestedFix suggested solution to resolve the issue
     */
    public InvalidConfigurationException(String configurationIssue, String suggestedFix) {
        super(buildErrorMessage(configurationIssue, suggestedFix));
        this.configurationIssue = configurationIssue;
        this.suggestedFix = suggestedFix;
    }
    
    /**
     * Constructs a new InvalidConfigurationException with issue, suggested fix, and cause.
     *
     * @param configurationIssue description of the configuration problem
     * @param suggestedFix suggested solution to resolve the issue
     * @param cause the underlying cause of the configuration issue
     */
    public InvalidConfigurationException(String configurationIssue, String suggestedFix, Throwable cause) {
        super(buildErrorMessage(configurationIssue, suggestedFix), cause);
        this.configurationIssue = configurationIssue;
        this.suggestedFix = suggestedFix;
    }
    
    /**
     * Returns the description of the configuration issue.
     *
     * @return the configuration issue description
     */
    public String getConfigurationIssue() {
        return configurationIssue;
    }
    
    /**
     * Returns the suggested fix for the configuration issue, if available.
     *
     * @return the suggested fix, or null if not provided
     */
    public String getSuggestedFix() {
        return suggestedFix;
    }
    
    /**
     * Returns whether a suggested fix is available for this configuration issue.
     *
     * @return true if a suggested fix is available, false otherwise
     */
    public boolean hasSuggestedFix() {
        return suggestedFix != null && !suggestedFix.trim().isEmpty();
    }
    
    private static String buildErrorMessage(String configurationIssue, String suggestedFix) {
        StringBuilder message = new StringBuilder();
        message.append("Invalid Grantly SDK configuration: ").append(configurationIssue);
        
        if (suggestedFix != null && !suggestedFix.trim().isEmpty()) {
            message.append("\n\nSuggested fix: ").append(suggestedFix);
        }
        
        return message.toString();
    }
    
    // Common configuration issues as static factory methods
    
    /**
     * Creates an InvalidConfigurationException for null callback configuration.
     *
     * @return configured exception for null callback
     */
    public static InvalidConfigurationException nullCallback() {
        return new InvalidConfigurationException(
            "Callback cannot be null",
            "Provide a valid GrantlyCallback implementation using setCallbacks()"
        );
    }
    
    /**
     * Creates an InvalidConfigurationException for empty permissions array.
     *
     * @return configured exception for empty permissions
     */
    public static InvalidConfigurationException emptyPermissions() {
        return new InvalidConfigurationException(
            "Permissions array cannot be null or empty",
            "Specify at least one permission using permissions() method"
        );
    }
    
    /**
     * Creates an InvalidConfigurationException for null context.
     *
     * @return configured exception for null context
     */
    public static InvalidConfigurationException nullContext() {
        return new InvalidConfigurationException(
            "Activity or Fragment context cannot be null",
            "Ensure the Activity/Fragment is not destroyed when making permission requests"
        );
    }
    
    /**
     * Creates an InvalidConfigurationException for invalid dialog theme.
     *
     * @param themeRes the invalid theme resource ID
     * @return configured exception for invalid theme
     */
    public static InvalidConfigurationException invalidDialogTheme(int themeRes) {
        return new InvalidConfigurationException(
            "Invalid dialog theme resource ID: " + themeRes,
            "Provide a valid theme resource ID or use 0 for default theme"
        );
    }
    
    /**
     * Creates an InvalidConfigurationException for conflicting configuration options.
     *
     * @param conflictDescription description of the conflicting options
     * @return configured exception for conflicting configuration
     */
    public static InvalidConfigurationException conflictingConfiguration(String conflictDescription) {
        return new InvalidConfigurationException(
            "Conflicting configuration options: " + conflictDescription,
            "Review your configuration and ensure options are compatible"
        );
    }
}