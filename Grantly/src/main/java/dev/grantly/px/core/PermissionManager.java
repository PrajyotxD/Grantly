package dev.grantly.px.core;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.ActivityCompat;

import dev.grantly.px.callback.GrantlyCallback;
import dev.grantly.px.model.PermissionResult;
import dev.grantly.px.model.PermissionState;
import dev.grantly.px.util.GrantlyLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core permission management class that orchestrates permission requests,
 * manages state, and handles callbacks. This class is responsible for
 * coordinating between different components of the permission system.
 */
public class PermissionManager {
    
    private static final String TAG = "PermissionManager";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private final PermissionChecker permissionChecker;
    private final Map<String, RequestContext> activeRequests;
    private final Handler mainHandler;
    
    // Singleton instance
    private static volatile PermissionManager instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    private PermissionManager() {
        this.permissionChecker = null; // Will be initialized with context
        this.activeRequests = new ConcurrentHashMap<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Constructor for dependency injection (used internally).
     *
     * @param permissionChecker The permission checker instance
     */
    public PermissionManager(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
        this.activeRequests = new ConcurrentHashMap<>();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Gets the singleton instance of PermissionManager.
     * Note: This requires proper initialization with context.
     *
     * @return The PermissionManager instance
     */
    public static PermissionManager getInstance() {
        if (instance == null) {
            synchronized (PermissionManager.class) {
                if (instance == null) {
                    instance = new PermissionManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Requests permissions with the given context and callback.
     *
     * @param context The request context containing all necessary information
     * @return The request ID for tracking this request
     */
    public String requestPermissions(RequestContext context) {
        if (context == null || !context.isActivityValid()) {
            GrantlyLogger.e(TAG, "Invalid request context or activity");
            throw new IllegalArgumentException("Invalid request context or activity");
        }
        
        String requestId = context.getRequestId();
        String[] permissions = context.getPermissions();
        
        GrantlyLogger.logPermissionRequestStart(TAG, requestId, permissions);
        GrantlyLogger.d(TAG, new GrantlyLogger.LogBuilder()
            .append("RequestID", requestId)
            .append("Lazy", context.isLazy())
            .append("PermissionCount", permissions.length)
            .build());
        
        // Check if there's already an active request for this activity
        if (hasActiveRequestForActivity(context.getActivity())) {
            GrantlyLogger.w(TAG, "Concurrent request detected for activity: " + context.getActivity().getClass().getSimpleName());
            // Handle concurrent request scenario
            handleConcurrentRequest(context);
            return requestId;
        }
        
        // Store the active request
        activeRequests.put(requestId, context);
        GrantlyLogger.d(TAG, "Stored active request: " + requestId + " (Total active: " + activeRequests.size() + ")");
        
        // Check current permission states
        List<PermissionResult> currentResults = checkPermissionStates(context.getPermissions());
        
        // Filter permissions that need to be requested
        List<String> permissionsToRequest = new ArrayList<>();
        List<PermissionResult> alreadyHandled = new ArrayList<>();
        
        for (PermissionResult result : currentResults) {
            if (result.getState() == PermissionState.GRANTED) {
                alreadyHandled.add(result);
            } else if (result.getState() == PermissionState.DENIED) {
                permissionsToRequest.add(result.getPermission());
            } else if (result.getState() == PermissionState.PERMANENTLY_DENIED) {
                // Handle permanently denied permissions
                if (!context.isLazy()) {
                    alreadyHandled.add(result);
                }
            } else if (result.getState() == PermissionState.REQUIRES_SPECIAL_HANDLING) {
                // Special permissions need different handling
                alreadyHandled.add(result);
            } else if (result.getState() == PermissionState.NOT_DECLARED) {
                // This is an error condition
                alreadyHandled.add(result);
            }
        }
        
        // If we have results to report immediately, do so
        if (!alreadyHandled.isEmpty() && permissionsToRequest.isEmpty()) {
            // All permissions are already handled, invoke callback immediately
            invokeCallback(context, currentResults);
            cleanup(requestId);
            return requestId;
        }
        
        // If there are permissions to request, proceed with the request
        if (!permissionsToRequest.isEmpty()) {
            String[] permissionsArray = permissionsToRequest.toArray(new String[0]);
            
            // Check if we should show rationale for any permissions
            boolean shouldShowRationale = false;
            for (String permission : permissionsArray) {
                if (permissionChecker.shouldShowRequestPermissionRationale(context.getActivity(), permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }
            
            if (shouldShowRationale && !context.isLazy()) {
                // Show rationale first, then request permissions
                // This would typically involve showing a dialog or UI
                // For now, we'll proceed directly to permission request
            }
            
            // Request the permissions
            ActivityCompat.requestPermissions(
                context.getActivity(),
                permissionsArray,
                PERMISSION_REQUEST_CODE
            );
        }
        
        return requestId;
    }
    
    /**
     * Handles the result of a permission request from the system.
     * This method should be called from the Activity's onRequestPermissionsResult.
     *
     * @param requestCode The request code passed to requestPermissions
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     * @return true if the result was handled, false otherwise
     */
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return false;
        }
        
        // Find the active request that matches these permissions
        RequestContext matchingContext = findMatchingRequest(permissions);
        if (matchingContext == null) {
            return false;
        }
        
        // Process the results
        List<PermissionResult> results = processPermissionResults(permissions, grantResults, matchingContext);
        
        // Invoke the callback
        invokeCallback(matchingContext, results);
        
        // Cleanup
        cleanup(matchingContext.getRequestId());
        
        return true;
    }
    
    /**
     * Cancels an active permission request.
     *
     * @param requestId The ID of the request to cancel
     * @return true if the request was cancelled, false if not found
     */
    public boolean cancelRequest(String requestId) {
        RequestContext context = activeRequests.get(requestId);
        if (context != null) {
            context.setInactive();
            cleanup(requestId);
            return true;
        }
        return false;
    }
    
    /**
     * Cancels all active requests for a specific activity.
     *
     * @param activity The activity whose requests should be cancelled
     * @return The number of requests cancelled
     */
    public int cancelRequestsForActivity(Activity activity) {
        int cancelledCount = 0;
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, RequestContext> entry : activeRequests.entrySet()) {
            if (entry.getValue().getActivity() == activity) {
                entry.getValue().setInactive();
                toRemove.add(entry.getKey());
                cancelledCount++;
            }
        }
        
        for (String requestId : toRemove) {
            cleanup(requestId);
        }
        
        return cancelledCount;
    }
    
    /**
     * Gets the current state of active requests.
     *
     * @return Map of request IDs to their contexts
     */
    public Map<String, RequestContext> getActiveRequests() {
        return new HashMap<>(activeRequests);
    }
    
    /**
     * Checks if there are any active requests.
     *
     * @return true if there are active requests, false otherwise
     */
    public boolean hasActiveRequests() {
        return !activeRequests.isEmpty();
    }
    
    /**
     * Checks if there's an active request for the given activity.
     *
     * @param activity The activity to check
     * @return true if there's an active request for this activity
     */
    public boolean hasActiveRequestForActivity(Activity activity) {
        for (RequestContext context : activeRequests.values()) {
            if (context.getActivity() == activity && context.isActive()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Cleans up expired or invalid requests.
     * This method should be called periodically to prevent memory leaks.
     */
    public void cleanupExpiredRequests() {
        List<String> toRemove = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long maxAge = 5 * 60 * 1000; // 5 minutes
        
        for (Map.Entry<String, RequestContext> entry : activeRequests.entrySet()) {
            RequestContext context = entry.getValue();
            if (!context.isActivityValid() || 
                context.getAge() > maxAge || 
                context.isCompleted()) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String requestId : toRemove) {
            cleanup(requestId);
        }
    }
    
    /**
     * Checks the current states of the given permissions.
     *
     * @param permissions Array of permissions to check
     * @return List of PermissionResult objects
     */
    private List<PermissionResult> checkPermissionStates(String[] permissions) {
        if (permissionChecker == null) {
            throw new IllegalStateException("PermissionChecker not initialized");
        }
        return permissionChecker.checkPermissions(permissions);
    }
    
    /**
     * Handles concurrent permission requests.
     *
     * @param newContext The new request context
     */
    private void handleConcurrentRequest(RequestContext newContext) {
        // For now, we'll reject concurrent requests
        // In a more sophisticated implementation, we might queue them
        GrantlyCallback callback = newContext.getCallback();
        if (callback != null) {
            mainHandler.post(() -> {
                List<PermissionResult> errorResults = new ArrayList<>();
                for (String permission : newContext.getPermissions()) {
                    errorResults.add(new PermissionResult(permission, PermissionState.DENIED, false));
                }
                callback.onPermissionResult(errorResults);
            });
        }
    }
    
    /**
     * Finds the active request that matches the given permissions.
     *
     * @param permissions The permissions to match
     * @return The matching RequestContext or null if not found
     */
    private RequestContext findMatchingRequest(String[] permissions) {
        for (RequestContext context : activeRequests.values()) {
            if (context.isActive() && containsAllPermissions(context.getPermissions(), permissions)) {
                return context;
            }
        }
        return null;
    }
    
    /**
     * Checks if the first array contains all permissions from the second array.
     *
     * @param contextPermissions The permissions from the context
     * @param resultPermissions The permissions from the result
     * @return true if all result permissions are in context permissions
     */
    private boolean containsAllPermissions(String[] contextPermissions, String[] resultPermissions) {
        for (String resultPermission : resultPermissions) {
            boolean found = false;
            for (String contextPermission : contextPermissions) {
                if (contextPermission.equals(resultPermission)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Processes the permission results from the system callback.
     *
     * @param permissions The requested permissions
     * @param grantResults The grant results
     * @param context The request context
     * @return List of PermissionResult objects
     */
    private List<PermissionResult> processPermissionResults(String[] permissions, int[] grantResults, RequestContext context) {
        List<PermissionResult> results = new ArrayList<>();
        
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            
            PermissionState state;
            boolean requiresRationale = false;
            
            if (granted) {
                state = PermissionState.GRANTED;
            } else {
                // Check if permanently denied
                boolean shouldShowRationale = permissionChecker.shouldShowRequestPermissionRationale(
                    context.getActivity(), permission);
                
                if (shouldShowRationale) {
                    state = PermissionState.DENIED;
                    requiresRationale = true;
                } else {
                    // Could be permanently denied or first time asking
                    // We'll assume permanently denied for now
                    state = PermissionState.PERMANENTLY_DENIED;
                }
            }
            
            results.add(new PermissionResult(permission, state, requiresRationale));
        }
        
        return results;
    }
    
    /**
     * Invokes the callback for the given context and results.
     *
     * @param context The request context
     * @param results The permission results
     */
    private void invokeCallback(RequestContext context, List<PermissionResult> results) {
        GrantlyCallback callback = context.getCallback();
        if (callback != null) {
            // Ensure callback is invoked on the main thread
            mainHandler.post(() -> callback.onPermissionResult(results));
        }
        
        // Mark context as completed
        context.setCompleted();
    }
    
    /**
     * Cleans up resources for the given request ID.
     *
     * @param requestId The request ID to clean up
     */
    private void cleanup(String requestId) {
        activeRequests.remove(requestId);
    }
}