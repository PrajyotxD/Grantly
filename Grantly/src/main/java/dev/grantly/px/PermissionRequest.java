package dev.grantly.px;

import android.app.Activity;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.grantly.px.callback.GrantlyCallback;
import dev.grantly.px.config.DenialBehavior;
import dev.grantly.px.core.PermissionManager;
import dev.grantly.px.core.RequestContext;
import dev.grantly.px.model.PermissionResult;
import dev.grantly.px.model.PermissionState;
import dev.grantly.px.provider.DialogProvider;
import dev.grantly.px.provider.RationaleProvider;

/**
 * Builder class for creating and configuring permission requests with a fluent API.
 * 
 * <p>This class provides a chainable interface for configuring permission requests with various
 * options including lazy loading, custom UI providers, rationale messages, and denial behaviors.
 * Once configured, the request can be executed to trigger the actual permission flow.</p>
 * 
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * Grantly.requestPermissions(this)
 *     .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
 *     .setCallbacks(new GrantlyCallback() {
 *         @Override
 *         public void onPermissionGranted(String[] permissions) {
 *             // Handle granted permissions
 *         }
 *         
 *         @Override
 *         public void onPermissionDenied(String[] permissions) {
 *             // Handle denied permissions
 *         }
 *     })
 *     .execute();
 * }</pre>
 * 
 * <h3>Advanced Configuration:</h3>
 * <pre>{@code
 * Grantly.requestPermissions(this)
 *     .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
 *     .setLazy(true)
 *     .setRationale("Location Access", "We need location to show nearby restaurants")
 *     .setDenialBehavior(DenialBehavior.CONTINUE_APP_FLOW)
 *     .setContinueOnDenied(true)
 *     .setCallbacks(callback)
 *     .execute();
 * }</pre>
 * 
 * <h3>Custom UI:</h3>
 * <pre>{@code
 * Grantly.requestPermissions(this)
 *     .permissions(Manifest.permission.READ_CONTACTS)
 *     .setCustomDialog(new MyCustomDialogProvider())
 *     .setRationale(new MyCustomRationaleProvider())
 *     .setCallbacks(callback)
 *     .execute();
 * }</pre>
 * 
 * <p><strong>Important:</strong> Each PermissionRequest instance can only be executed once.
 * Attempting to modify or execute an already-executed request will throw an IllegalStateException.</p>
 * 
 * <p><strong>Thread Safety:</strong> This class is not thread-safe. All method calls should be
 * made from the same thread, typically the main/UI thread.</p>
 * 
 * @since 1.0.0
 * @see Grantly#requestPermissions(Activity)
 * @see Grantly#requestPermissions(Fragment)
 * @see GrantlyCallback
 * @see RationaleProvider
 * @see DialogProvider
 * @see DenialBehavior
 */
public class PermissionRequest {
    
    private final Activity activity;
    private final Fragment fragment;
    
    // Configuration fields
    private final Set<String> permissions = new HashSet<>();
    private boolean lazy = false;
    private String rationaleTitle = null;
    private String rationaleMessage = null;
    private RationaleProvider rationaleProvider = null;
    private DialogProvider dialogProvider = null;
    private int customDialogLayoutRes = 0;
    private boolean continueOnDenied = true;
    private GrantlyCallback callback = null;
    private DenialBehavior denialBehavior = null;
    
    // Internal state
    private boolean executed = false;

    /**
     * Package-private constructor for Activity-based requests.
     * Use Grantly.requestPermissions(Activity) to create instances.
     */
    PermissionRequest(@NonNull Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    /**
     * Package-private constructor for Fragment-based requests.
     * Use Grantly.requestPermissions(Fragment) to create instances.
     */
    PermissionRequest(@NonNull Fragment fragment) {
        this.fragment = fragment;
        this.activity = fragment.getActivity();
    }

    /**
     * Specify the permissions to request.
     * 
     * <p>This method replaces any previously set permissions. All permissions must be declared
     * in the app's AndroidManifest.xml file, otherwise a {@link dev.grantly.px.exception.PermissionNotDeclaredException}
     * will be thrown during execution.</p>
     * 
     * <p>Example:</p>
     * <pre>{@code
     * .permissions(
     *     Manifest.permission.CAMERA,
     *     Manifest.permission.RECORD_AUDIO,
     *     Manifest.permission.WRITE_EXTERNAL_STORAGE
     * )
     * }</pre>
     * 
     * @param permissions Array of permission strings to request (e.g., Manifest.permission.CAMERA)
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if permissions array is null or empty
     * @throws IllegalStateException if this request has already been executed
     * @see #addPermissions(String...)
     * @since 1.0.0
     */
    public PermissionRequest permissions(@NonNull String... permissions) {
        validateNotExecuted();
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("Permissions array cannot be null or empty");
        }
        
        this.permissions.clear();
        this.permissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * Add additional permissions to the request.
     * 
     * @param permissions Array of additional permission strings to request
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if permissions array is null or empty
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest addPermissions(@NonNull String... permissions) {
        validateNotExecuted();
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("Permissions array cannot be null or empty");
        }
        
        this.permissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * Set whether this permission request should use lazy mode.
     * 
     * <p>In lazy mode, permissions are only requested when the feature is actually used,
     * providing a better user experience by requesting permissions in context. In eager mode,
     * permissions are requested immediately when execute() is called.</p>
     * 
     * <p><strong>Lazy Mode Benefits:</strong></p>
     * <ul>
     *   <li>Better user experience - permissions requested in context</li>
     *   <li>Higher grant rates - users understand why permission is needed</li>
     *   <li>Reduced permission fatigue - fewer upfront permission requests</li>
     * </ul>
     * 
     * <p>Example:</p>
     * <pre>{@code
     * // Request camera permission only when user taps "Take Photo"
     * private void onTakePhotoClicked() {
     *     Grantly.requestPermissions(this)
     *         .permissions(Manifest.permission.CAMERA)
     *         .setLazy(true)
     *         .setCallbacks(callback)
     *         .execute();
     * }
     * }</pre>
     * 
     * @param lazy true to enable lazy mode (request when needed), 
     *             false for eager mode (request immediately)
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalStateException if this request has already been executed
     * @since 1.0.0
     */
    public PermissionRequest setLazy(boolean lazy) {
        validateNotExecuted();
        this.lazy = lazy;
        return this;
    }

    /**
     * Set a simple rationale with title and message.
     * This will be shown to users when they need an explanation for why permissions are needed.
     * 
     * @param title The title for the rationale dialog
     * @param message The message explaining why permissions are needed
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if title or message is null or empty
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setRationale(@NonNull String title, @NonNull String message) {
        validateNotExecuted();
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Rationale title cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Rationale message cannot be null or empty");
        }
        
        this.rationaleTitle = title;
        this.rationaleMessage = message;
        this.rationaleProvider = null; // Clear custom provider if set
        return this;
    }

    /**
     * Set a custom rationale provider for showing permission explanations.
     * 
     * @param provider The custom rationale provider implementation
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if provider is null
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setRationale(@NonNull RationaleProvider provider) {
        validateNotExecuted();
        if (provider == null) {
            throw new IllegalArgumentException("RationaleProvider cannot be null");
        }
        
        this.rationaleProvider = provider;
        this.rationaleTitle = null; // Clear simple rationale if set
        this.rationaleMessage = null;
        return this;
    }

    /**
     * Set a custom dialog provider for permission requests.
     * 
     * @param provider The custom dialog provider implementation
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if provider is null
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setCustomDialog(@NonNull DialogProvider provider) {
        validateNotExecuted();
        if (provider == null) {
            throw new IllegalArgumentException("DialogProvider cannot be null");
        }
        
        this.dialogProvider = provider;
        this.customDialogLayoutRes = 0; // Clear layout resource if set
        return this;
    }

    /**
     * Set a custom dialog layout resource for permission requests.
     * 
     * @param layoutRes The layout resource ID for the custom dialog
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if layoutRes is not a valid resource ID
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setCustomDialog(@LayoutRes int layoutRes) {
        validateNotExecuted();
        if (layoutRes <= 0) {
            throw new IllegalArgumentException("Layout resource ID must be a valid positive integer");
        }
        
        this.customDialogLayoutRes = layoutRes;
        this.dialogProvider = null; // Clear custom provider if set
        return this;
    }

    /**
     * Set whether the app should continue normal flow when permissions are denied.
     * 
     * @param continueOnDenied true to continue app flow on denial, false to handle denial specially
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setContinueOnDenied(boolean continueOnDenied) {
        validateNotExecuted();
        this.continueOnDenied = continueOnDenied;
        return this;
    }

    /**
     * Set the callback to handle permission request results.
     * 
     * @param callback The callback implementation to handle results
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if callback is null
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setCallbacks(@NonNull GrantlyCallback callback) {
        validateNotExecuted();
        if (callback == null) {
            throw new IllegalArgumentException("GrantlyCallback cannot be null");
        }
        
        this.callback = callback;
        return this;
    }

    /**
     * Set the behavior when permissions are denied.
     * 
     * @param behavior The denial behavior to use for this request
     * @return this PermissionRequest instance for method chaining
     * @throws IllegalArgumentException if behavior is null
     * @throws IllegalStateException if this request has already been executed
     */
    public PermissionRequest setDenialBehavior(@NonNull DenialBehavior behavior) {
        validateNotExecuted();
        if (behavior == null) {
            throw new IllegalArgumentException("DenialBehavior cannot be null");
        }
        
        this.denialBehavior = behavior;
        return this;
    }

    /**
     * Execute the permission request with the configured settings.
     * This method can only be called once per PermissionRequest instance.
     * 
     * @throws IllegalStateException if no permissions are specified, no callback is set,
     *                               the request has already been executed, or the activity is null
     */
    public void execute() {
        validateNotExecuted();
        validateConfiguration();
        
        this.executed = true;
        
        try {
            // Create request context
            RequestContext context = new RequestContext(
                activity,
                getPermissions(),
                new CallbackAdapter(callback),
                lazy,
                this
            );
            
            // Get PermissionManager instance and execute request
            PermissionManager permissionManager = Grantly.getPermissionManager();
            permissionManager.requestPermissions(context);
            
        } catch (Exception e) {
            // Handle any errors during execution
            if (callback != null) {
                callback.onPermissionRequestCancelled();
            }
            throw new RuntimeException("Failed to execute permission request", e);
        }
    }

    /**
     * Validate that this request has not been executed yet.
     * 
     * @throws IllegalStateException if the request has already been executed
     */
    private void validateNotExecuted() {
        if (executed) {
            throw new IllegalStateException("This PermissionRequest has already been executed and cannot be modified");
        }
    }

    /**
     * Validate the configuration before execution.
     * 
     * @throws IllegalStateException if the configuration is invalid
     */
    private void validateConfiguration() {
        if (permissions.isEmpty()) {
            throw new IllegalStateException("No permissions specified. Call permissions() before execute()");
        }
        
        if (callback == null) {
            throw new IllegalStateException("No callback specified. Call setCallbacks() before execute()");
        }
        
        if (activity == null) {
            throw new IllegalStateException("Activity is null. Cannot execute permission request");
        }
        
        // Validate that we don't have conflicting rationale configurations
        if (rationaleProvider != null && (rationaleTitle != null || rationaleMessage != null)) {
            throw new IllegalStateException("Cannot set both custom RationaleProvider and simple rationale title/message");
        }
        
        // Validate that we don't have conflicting dialog configurations
        if (dialogProvider != null && customDialogLayoutRes > 0) {
            throw new IllegalStateException("Cannot set both custom DialogProvider and custom dialog layout resource");
        }
    }

    // Getters for internal use by the SDK
    
    Activity getActivity() {
        return activity;
    }
    
    Fragment getFragment() {
        return fragment;
    }
    
    String[] getPermissions() {
        return permissions.toArray(new String[0]);
    }
    
    boolean isLazy() {
        return lazy;
    }
    
    String getRationaleTitle() {
        return rationaleTitle;
    }
    
    String getRationaleMessage() {
        return rationaleMessage;
    }
    
    RationaleProvider getRationaleProvider() {
        return rationaleProvider;
    }
    
    DialogProvider getDialogProvider() {
        return dialogProvider;
    }
    
    int getCustomDialogLayoutRes() {
        return customDialogLayoutRes;
    }
    
    boolean shouldContinueOnDenied() {
        return continueOnDenied;
    }
    
    GrantlyCallback getCallback() {
        return callback;
    }
    
    DenialBehavior getDenialBehavior() {
        return denialBehavior;
    }
    
    boolean isExecuted() {
        return executed;
    }
    
    /**
     * Adapter class to convert between the detailed PermissionResult callback
     * and the simplified GrantlyCallback interface.
     */
    private static class CallbackAdapter implements dev.grantly.px.callback.GrantlyCallback {
        private final GrantlyCallback originalCallback;
        
        CallbackAdapter(GrantlyCallback originalCallback) {
            this.originalCallback = originalCallback;
        }
        
        @Override
        public void onPermissionGranted(String[] permissions) {
            if (originalCallback != null) {
                originalCallback.onPermissionGranted(permissions);
            }
        }
        
        @Override
        public void onPermissionDenied(String[] permissions) {
            if (originalCallback != null) {
                originalCallback.onPermissionDenied(permissions);
            }
        }
        
        @Override
        public void onPermissionPermanentlyDenied(String[] permissions) {
            if (originalCallback != null) {
                originalCallback.onPermissionPermanentlyDenied(permissions);
            }
        }
        
        @Override
        public void onPermissionRequestCancelled() {
            if (originalCallback != null) {
                originalCallback.onPermissionRequestCancelled();
            }
        }
        
        @Override
        public void onPermissionResult(List<PermissionResult> results) {
            if (originalCallback == null || results == null || results.isEmpty()) {
                return;
            }
            
            // Group results by state
            List<String> granted = new ArrayList<>();
            List<String> denied = new ArrayList<>();
            List<String> permanentlyDenied = new ArrayList<>();
            
            for (PermissionResult result : results) {
                switch (result.getState()) {
                    case GRANTED:
                        granted.add(result.getPermission());
                        break;
                    case DENIED:
                        denied.add(result.getPermission());
                        break;
                    case PERMANENTLY_DENIED:
                        permanentlyDenied.add(result.getPermission());
                        break;
                    case NOT_DECLARED:
                    case REQUIRES_SPECIAL_HANDLING:
                        // These are handled as denied for now
                        denied.add(result.getPermission());
                        break;
                }
            }
            
            // Call appropriate callback methods based on results
            if (!granted.isEmpty()) {
                originalCallback.onPermissionGranted(granted.toArray(new String[0]));
            }
            
            if (!denied.isEmpty()) {
                originalCallback.onPermissionDenied(denied.toArray(new String[0]));
            }
            
            if (!permanentlyDenied.isEmpty()) {
                originalCallback.onPermissionPermanentlyDenied(permanentlyDenied.toArray(new String[0]));
            }
        }
    }
}