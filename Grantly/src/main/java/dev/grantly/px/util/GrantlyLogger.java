package dev.grantly.px.util;

import android.util.Log;

import java.util.Locale;

/**
 * Centralized logging system for the Grantly SDK.
 * 
 * Provides configurable logging with different log levels, meaningful messages
 * for troubleshooting, and debug information for permission state changes and flows.
 */
public class GrantlyLogger {
    
    private static final String SDK_TAG_PREFIX = "Grantly";
    private static final String SEPARATOR = " | ";
    
    // Log levels
    public static final int VERBOSE = Log.VERBOSE;
    public static final int DEBUG = Log.DEBUG;
    public static final int INFO = Log.INFO;
    public static final int WARN = Log.WARN;
    public static final int ERROR = Log.ERROR;
    
    // Configuration
    private static boolean loggingEnabled = false;
    private static int minimumLogLevel = DEBUG;
    private static LogFormatter logFormatter = new DefaultLogFormatter();
    
    /**
     * Enables or disables logging for the Grantly SDK.
     *
     * @param enabled true to enable logging, false to disable
     */
    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
        if (enabled) {
            i("GrantlyLogger", "Grantly SDK logging enabled");
        }
    }
    
    /**
     * Checks if logging is currently enabled.
     *
     * @return true if logging is enabled, false otherwise
     */
    public static boolean isLoggingEnabled() {
        return loggingEnabled;
    }
    
    /**
     * Sets the minimum log level. Messages below this level will not be logged.
     *
     * @param level the minimum log level (VERBOSE, DEBUG, INFO, WARN, ERROR)
     */
    public static void setMinimumLogLevel(int level) {
        minimumLogLevel = level;
        d("GrantlyLogger", "Minimum log level set to: " + levelToString(level));
    }
    
    /**
     * Gets the current minimum log level.
     *
     * @return the current minimum log level
     */
    public static int getMinimumLogLevel() {
        return minimumLogLevel;
    }
    
    /**
     * Sets a custom log formatter for formatting log messages.
     *
     * @param formatter the custom log formatter, or null to use default
     */
    public static void setLogFormatter(LogFormatter formatter) {
        logFormatter = formatter != null ? formatter : new DefaultLogFormatter();
    }
    
    // Verbose logging
    
    /**
     * Logs a verbose message.
     *
     * @param tag the log tag
     * @param message the log message
     */
    public static void v(String tag, String message) {
        log(VERBOSE, tag, message, null);
    }
    
    /**
     * Logs a verbose message with throwable.
     *
     * @param tag the log tag
     * @param message the log message
     * @param throwable the throwable to log
     */
    public static void v(String tag, String message, Throwable throwable) {
        log(VERBOSE, tag, message, throwable);
    }
    
    // Debug logging
    
    /**
     * Logs a debug message.
     *
     * @param tag the log tag
     * @param message the log message
     */
    public static void d(String tag, String message) {
        log(DEBUG, tag, message, null);
    }
    
    /**
     * Logs a debug message with throwable.
     *
     * @param tag the log tag
     * @param message the log message
     * @param throwable the throwable to log
     */
    public static void d(String tag, String message, Throwable throwable) {
        log(DEBUG, tag, message, throwable);
    }
    
    // Info logging
    
    /**
     * Logs an info message.
     *
     * @param tag the log tag
     * @param message the log message
     */
    public static void i(String tag, String message) {
        log(INFO, tag, message, null);
    }
    
    /**
     * Logs an info message with throwable.
     *
     * @param tag the log tag
     * @param message the log message
     * @param throwable the throwable to log
     */
    public static void i(String tag, String message, Throwable throwable) {
        log(INFO, tag, message, throwable);
    }
    
    // Warning logging
    
    /**
     * Logs a warning message.
     *
     * @param tag the log tag
     * @param message the log message
     */
    public static void w(String tag, String message) {
        log(WARN, tag, message, null);
    }
    
    /**
     * Logs a warning message with throwable.
     *
     * @param tag the log tag
     * @param message the log message
     * @param throwable the throwable to log
     */
    public static void w(String tag, String message, Throwable throwable) {
        log(WARN, tag, message, throwable);
    }
    
    // Error logging
    
    /**
     * Logs an error message.
     *
     * @param tag the log tag
     * @param message the log message
     */
    public static void e(String tag, String message) {
        log(ERROR, tag, message, null);
    }
    
    /**
     * Logs an error message with throwable.
     *
     * @param tag the log tag
     * @param message the log message
     * @param throwable the throwable to log
     */
    public static void e(String tag, String message, Throwable throwable) {
        log(ERROR, tag, message, throwable);
    }
    
    // Specialized logging methods for permission flows
    
    /**
     * Logs permission state changes with detailed information.
     *
     * @param tag the log tag
     * @param permission the permission that changed state
     * @param oldState the previous state
     * @param newState the new state
     */
    public static void logPermissionStateChange(String tag, String permission, 
                                              String oldState, String newState) {
        if (!loggingEnabled || INFO < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Permission state change: %s [%s → %s]", 
            permission, oldState, newState);
        log(INFO, tag, message, null);
    }
    
    /**
     * Logs the start of a permission request flow.
     *
     * @param tag the log tag
     * @param requestId unique identifier for the request
     * @param permissions array of permissions being requested
     */
    public static void logPermissionRequestStart(String tag, String requestId, String[] permissions) {
        if (!loggingEnabled || DEBUG < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Starting permission request [ID: %s] for permissions: %s", 
            requestId, String.join(", ", permissions));
        log(DEBUG, tag, message, null);
    }
    
    /**
     * Logs the completion of a permission request flow.
     *
     * @param tag the log tag
     * @param requestId unique identifier for the request
     * @param granted array of granted permissions
     * @param denied array of denied permissions
     */
    public static void logPermissionRequestComplete(String tag, String requestId, 
                                                  String[] granted, String[] denied) {
        if (!loggingEnabled || INFO < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Permission request completed [ID: %s] - Granted: [%s], Denied: [%s]", 
            requestId, 
            granted.length > 0 ? String.join(", ", granted) : "none",
            denied.length > 0 ? String.join(", ", denied) : "none");
        log(INFO, tag, message, null);
    }
    
    /**
     * Logs rationale dialog events.
     *
     * @param tag the log tag
     * @param action the action taken (shown, accepted, denied)
     * @param permissions the permissions involved
     */
    public static void logRationaleDialog(String tag, String action, String[] permissions) {
        if (!loggingEnabled || DEBUG < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Rationale dialog %s for permissions: %s", 
            action, String.join(", ", permissions));
        log(DEBUG, tag, message, null);
    }
    
    /**
     * Logs special permission handling events.
     *
     * @param tag the log tag
     * @param permission the special permission
     * @param action the action taken
     * @param details additional details about the action
     */
    public static void logSpecialPermission(String tag, String permission, String action, String details) {
        if (!loggingEnabled || DEBUG < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Special permission [%s] %s: %s", 
            permission, action, details);
        log(DEBUG, tag, message, null);
    }
    
    /**
     * Logs configuration changes and validation.
     *
     * @param tag the log tag
     * @param configType the type of configuration
     * @param details details about the configuration change
     */
    public static void logConfiguration(String tag, String configType, String details) {
        if (!loggingEnabled || DEBUG < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Configuration [%s]: %s", configType, details);
        log(DEBUG, tag, message, null);
    }
    
    /**
     * Logs performance metrics for troubleshooting.
     *
     * @param tag the log tag
     * @param operation the operation being measured
     * @param durationMs the duration in milliseconds
     */
    public static void logPerformance(String tag, String operation, long durationMs) {
        if (!loggingEnabled || VERBOSE < minimumLogLevel) return;
        
        String message = String.format(Locale.US, 
            "Performance [%s]: %d ms", operation, durationMs);
        log(VERBOSE, tag, message, null);
    }
    
    // Core logging method
    
    private static void log(int level, String tag, String message, Throwable throwable) {
        if (!loggingEnabled || level < minimumLogLevel) {
            return;
        }
        
        String formattedTag = SDK_TAG_PREFIX + SEPARATOR + tag;
        String formattedMessage = logFormatter.format(level, tag, message, throwable);
        
        switch (level) {
            case VERBOSE:
                if (throwable != null) {
                    Log.v(formattedTag, formattedMessage, throwable);
                } else {
                    Log.v(formattedTag, formattedMessage);
                }
                break;
            case DEBUG:
                if (throwable != null) {
                    Log.d(formattedTag, formattedMessage, throwable);
                } else {
                    Log.d(formattedTag, formattedMessage);
                }
                break;
            case INFO:
                if (throwable != null) {
                    Log.i(formattedTag, formattedMessage, throwable);
                } else {
                    Log.i(formattedTag, formattedMessage);
                }
                break;
            case WARN:
                if (throwable != null) {
                    Log.w(formattedTag, formattedMessage, throwable);
                } else {
                    Log.w(formattedTag, formattedMessage);
                }
                break;
            case ERROR:
                if (throwable != null) {
                    Log.e(formattedTag, formattedMessage, throwable);
                } else {
                    Log.e(formattedTag, formattedMessage);
                }
                break;
        }
    }
    
    private static String levelToString(int level) {
        switch (level) {
            case VERBOSE: return "VERBOSE";
            case DEBUG: return "DEBUG";
            case INFO: return "INFO";
            case WARN: return "WARN";
            case ERROR: return "ERROR";
            default: return "UNKNOWN";
        }
    }
    
    /**
     * Interface for custom log message formatting.
     */
    public interface LogFormatter {
        /**
         * Formats a log message.
         *
         * @param level the log level
         * @param tag the log tag
         * @param message the original message
         * @param throwable the throwable, if any
         * @return the formatted message
         */
        String format(int level, String tag, String message, Throwable throwable);
    }
    
    /**
     * Default log formatter implementation.
     */
    private static class DefaultLogFormatter implements LogFormatter {
        @Override
        public String format(int level, String tag, String message, Throwable throwable) {
            StringBuilder formatted = new StringBuilder();
            
            // Add timestamp for debug builds
            if (level <= DEBUG) {
                formatted.append("[").append(System.currentTimeMillis()).append("] ");
            }
            
            formatted.append(message);
            
            // Add thread information for verbose logging
            if (level == VERBOSE) {
                formatted.append(" [Thread: ").append(Thread.currentThread().getName()).append("]");
            }
            
            return formatted.toString();
        }
    }
    
    /**
     * Utility class for creating structured log messages.
     */
    public static class LogBuilder {
        private final StringBuilder message;
        
        public LogBuilder() {
            this.message = new StringBuilder();
        }
        
        public LogBuilder append(String key, Object value) {
            if (message.length() > 0) {
                message.append(", ");
            }
            message.append(key).append("=").append(value);
            return this;
        }
        
        public LogBuilder append(String text) {
            if (message.length() > 0) {
                message.append(" ");
            }
            message.append(text);
            return this;
        }
        
        public String build() {
            return message.toString();
        }
    }
}