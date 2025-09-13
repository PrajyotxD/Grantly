package dev.grantly.px.core;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import dev.grantly.px.callback.GrantlyCallback;
import dev.grantly.px.config.DenialBehavior;
import dev.grantly.px.exception.GrantlyException;
import dev.grantly.px.exception.InvalidConfigurationException;
import dev.grantly.px.exception.PermissionNotDeclaredException;
import dev.grantly.px.exception.PermissionRequestInProgressException;
import dev.grantly.px.util.GrantlyLogger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages error recovery strategies and graceful degradation for the Grantly SDK.
 * 
 * This class provides centralized error handling, retry mechanisms, and fallback
 * strategies to ensure the SDK behaves gracefully even when errors occur.
 */
class ErrorRecoveryManager {
    
    private static final String TAG = "ErrorRecoveryManager";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 second
    private static final long MAX_RETRY_DELAY_MS = 8000; // 8 seconds
    
    private final Handler mainHandler;
    private final AtomicInteger retryCount;
    
    /**
     * Constructs a new ErrorRecoveryManager.
     */
    ErrorRecoveryManager() {
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.retryCount = new AtomicInteger(0);
    }
    
    /**
     * Handles exceptions that occur during permission operations with appropriate
     * recovery strategies.
     *
     * @param context the context where the error occurred
     * @param exception the exception that was thrown
     * @param callback the callback to notify about the error or recovery
     * @param denialBehavior the configured behavior for handling denials/errors
     * @return true if the error was handled and recovery was attempted, false otherwise
     */
    boolean handleException(Context context, GrantlyException exception, 
                           GrantlyCallback callback, DenialBehavior denialBehavior) {
        GrantlyLogger.d(TAG, "Handling exception: " + exception.getClass().getSimpleName());
        
        try {
            if (exception instanceof PermissionNotDeclaredException) {
                return handlePermissionNotDeclaredException(context, 
                    (PermissionNotDeclaredException) exception, callback, denialBehavior);
            } else if (exception instanceof InvalidConfigurationException) {
                return handleInvalidConfigurationException(context, 
                    (InvalidConfigurationException) exception, callback, denialBehavior);
            } else if (exception instanceof PermissionRequestInProgressException) {
                return handlePermissionRequestInProgressException(context, 
                    (PermissionRequestInProgressException) exception, callback, denialBehavior);
            } else {
                return handleGenericException(context, exception, callback, denialBehavior);
            }
        } catch (Exception recoveryException) {
            GrantlyLogger.e(TAG, "Error during exception recovery", recoveryException);
            // Fallback to generic error handling
            notifyErrorCallback(callback, exception);
            return false;
        }
    }
    
    /**
     * Attempts to retry a failed operation with exponential backoff.
     *
     * @param operation the operation to retry
     * @param maxAttempts maximum number of retry attempts
     * @param onSuccess callback for successful retry
     * @param onFailure callback for final failure after all retries
     */
    void retryWithBackoff(Runnable operation, int maxAttempts, 
                         Runnable onSuccess, Runnable onFailure) {
        int currentAttempt = retryCount.incrementAndGet();
        
        if (currentAttempt > maxAttempts) {
            GrantlyLogger.w(TAG, "Max retry attempts exceeded: " + maxAttempts);
            retryCount.set(0);
            if (onFailure != null) {
                onFailure.run();
            }
            return;
        }
        
        long delay = calculateRetryDelay(currentAttempt);
        GrantlyLogger.d(TAG, "Retrying operation (attempt " + currentAttempt + ") after " + delay + "ms");
        
        mainHandler.postDelayed(() -> {
            try {
                operation.run();
                retryCount.set(0); // Reset on success
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                GrantlyLogger.w(TAG, "Retry attempt " + currentAttempt + " failed", e);
                retryWithBackoff(operation, maxAttempts, onSuccess, onFailure);
            }
        }, delay);
    }
    
    /**
     * Provides graceful degradation when critical operations fail.
     *
     * @param context the context for the degradation
     * @param callback the callback to notify about the degradation
     * @param denialBehavior the configured behavior for handling failures
     */
    void gracefulDegradation(Context context, GrantlyCallback callback, DenialBehavior denialBehavior) {
        GrantlyLogger.i(TAG, "Applying graceful degradation with behavior: " + denialBehavior);
        
        switch (denialBehavior) {
            case CONTINUE_APP_FLOW:
                // Continue with limited functionality
                GrantlyLogger.d(TAG, "Continuing app flow with limited functionality");
                if (callback != null) {
                    callback.onPermissionDenied(new String[]{"DEGRADED_FUNCTIONALITY"});
                }
                break;
                
            case DISABLE_FEATURE:
                // Disable the feature that requires permissions
                GrantlyLogger.d(TAG, "Disabling feature due to permission failure");
                if (callback != null) {
                    callback.onPermissionDenied(new String[]{"FEATURE_DISABLED"});
                }
                break;
                
            case EXIT_APP_WITH_DIALOG:
                // Show dialog and potentially exit
                GrantlyLogger.d(TAG, "Showing exit dialog due to critical permission failure");
                showExitDialog(context, callback);
                break;
                
            case EXIT_APP_IMMEDIATELY:
                // Immediate exit (should be used carefully)
                GrantlyLogger.w(TAG, "Immediate app exit due to critical permission failure");
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
                break;
        }
    }
    
    private boolean handlePermissionNotDeclaredException(Context context, 
            PermissionNotDeclaredException exception, GrantlyCallback callback, 
            DenialBehavior denialBehavior) {
        GrantlyLogger.e(TAG, "Permission not declared: " + exception.getUndeclaredPermissionsString());
        
        // This is a developer error that cannot be recovered from at runtime
        // Log detailed information for debugging
        GrantlyLogger.e(TAG, exception.getResolutionGuidance());
        
        // Notify callback about the error
        if (callback != null) {
            callback.onPermissionDenied(exception.getUndeclaredPermissions().toArray(new String[0]));
        }
        
        // Apply graceful degradation based on denial behavior
        gracefulDegradation(context, callback, denialBehavior);
        
        return false; // Cannot recover from this error
    }
    
    private boolean handleInvalidConfigurationException(Context context, 
            InvalidConfigurationException exception, GrantlyCallback callback, 
            DenialBehavior denialBehavior) {
        GrantlyLogger.e(TAG, "Invalid configuration: " + exception.getConfigurationIssue());
        
        if (exception.hasSuggestedFix()) {
            GrantlyLogger.i(TAG, "Suggested fix: " + exception.getSuggestedFix());
        }
        
        // Some configuration errors might be recoverable with defaults
        if (isRecoverableConfigurationError(exception)) {
            GrantlyLogger.i(TAG, "Attempting to recover with default configuration");
            return true; // Indicate recovery was attempted
        }
        
        // Non-recoverable configuration error
        notifyErrorCallback(callback, exception);
        gracefulDegradation(context, callback, denialBehavior);
        
        return false;
    }
    
    private boolean handlePermissionRequestInProgressException(Context context, 
            PermissionRequestInProgressException exception, GrantlyCallback callback, 
            DenialBehavior denialBehavior) {
        GrantlyLogger.w(TAG, "Permission request in progress: " + exception.getActiveRequestId());
        
        // Check if the active request has been running too long (potential stuck request)
        if (exception.getRequestDuration() > 30000) { // 30 seconds
            GrantlyLogger.w(TAG, "Active request appears to be stuck, attempting recovery");
            // Could implement stuck request recovery here
            return true;
        }
        
        // For normal concurrent requests, just notify and wait
        GrantlyLogger.d(TAG, exception.getResolutionGuidance());
        notifyErrorCallback(callback, exception);
        
        return false; // Cannot recover immediately, need to wait
    }
    
    private boolean handleGenericException(Context context, GrantlyException exception, 
            GrantlyCallback callback, DenialBehavior denialBehavior) {
        GrantlyLogger.e(TAG, "Generic Grantly exception occurred", exception);
        
        // Attempt retry for potentially transient errors
        if (isRetriableError(exception)) {
            GrantlyLogger.i(TAG, "Error appears retriable, scheduling retry");
            // Implementation would depend on the specific operation context
            return true;
        }
        
        // Non-retriable error
        notifyErrorCallback(callback, exception);
        gracefulDegradation(context, callback, denialBehavior);
        
        return false;
    }
    
    private void showExitDialog(Context context, GrantlyCallback callback) {
        // This would show a dialog explaining why the app needs to exit
        // Implementation would use the DialogProvider system
        GrantlyLogger.d(TAG, "Exit dialog would be shown here");
        
        // For now, just notify the callback
        if (callback != null) {
            callback.onPermissionRequestCancelled();
        }
    }
    
    private void notifyErrorCallback(GrantlyCallback callback, GrantlyException exception) {
        if (callback != null) {
            // Use the cancelled callback as a generic error notification
            callback.onPermissionRequestCancelled();
        }
    }
    
    private boolean isRecoverableConfigurationError(InvalidConfigurationException exception) {
        String issue = exception.getConfigurationIssue();
        
        // Some configuration errors that might be recoverable with defaults
        return issue != null && (
            issue.contains("theme") ||
            issue.contains("layout") ||
            issue.contains("style")
        );
    }
    
    private boolean isRetriableError(GrantlyException exception) {
        // Determine if an error is worth retrying
        // This could be based on the exception type or message
        return exception.getCause() != null && 
               (exception.getCause() instanceof SecurityException ||
                exception.getCause() instanceof IllegalStateException);
    }
    
    private long calculateRetryDelay(int attemptNumber) {
        // Exponential backoff with jitter
        long delay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attemptNumber - 1);
        delay = Math.min(delay, MAX_RETRY_DELAY_MS);
        
        // Add some jitter to prevent thundering herd
        long jitter = (long) (delay * 0.1 * Math.random());
        return delay + jitter;
    }
    
    /**
     * Resets the retry counter. Should be called when starting a new operation.
     */
    void resetRetryCount() {
        retryCount.set(0);
    }
    
    /**
     * Gets the current retry count.
     *
     * @return the current retry count
     */
    int getCurrentRetryCount() {
        return retryCount.get();
    }
}