package dev.grantly.px;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.grantly.px.config.GrantlyConfig;
import dev.grantly.px.core.ManifestParser;
import dev.grantly.px.core.PermissionChecker;
import dev.grantly.px.core.PermissionManager;
import dev.grantly.px.core.SpecialPermissionHandler;
import dev.grantly.px.exception.GrantlyException;
import dev.grantly.px.util.GrantlyLogger;


/**
 * Main entry point for the Grantly Android Permission Management SDK.
 * 
 * <p>Grantly is a comprehensive Android Permission Management SDK that simplifies runtime 
 * permission handling across different Android versions. It provides automatic manifest parsing, 
 * customizable UI components, robust error handling, and support for special permissions.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Automatic manifest parsing - detects dangerous permissions from AndroidManifest.xml</li>
 *   <li>Customizable UI - fully customizable dialogs, toasts, and rationale screens</li>
 *   <li>Flexible configuration - support for lazy/eager requests and configurable denial behaviors</li>
 *   <li>Special permissions - built-in support for overlay, settings, location, and notification permissions</li>
 *   <li>Robust error handling - comprehensive exception handling with meaningful error messages</li>
 * </ul>
 * 
 * <h3>Basic Usage:</h3>
 * <pre>{@code
 * // Simple permission request
 * Grantly.requestPermissions(this)
 *     .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
 *     .setCallbacks(new GrantlyCallback() {
 *         @Override
 *         public void onPermissionGranted(String[] permissions) {
 *             // Permissions granted - proceed with functionality
 *             startCamera();
 *         }
 *         
 *         @Override
 *         public void onPermissionDenied(String[] permissions) {
 *             // Handle denied permissions
 *             showFeatureUnavailableMessage();
 *         }
 *         
 *         @Override
 *         public void onPermissionPermanentlyDenied(String[] permissions) {
 *             // Guide user to app settings
 *             GrantlyUtils.openAppSettings(MainActivity.this);
 *         }
 *     })
 *     .execute();
 * }</pre>
 * 
 * <h3>Lazy Permission Requests:</h3>
 * <pre>{@code
 * // Request permissions only when needed
 * private void takePicture() {
 *     Grantly.requestPermissions(this)
 *         .permissions(Manifest.permission.CAMERA)
 *         .setLazy(true)
 *         .setRationale("Camera Access", "We need camera permission to take photos")
 *         .setCallbacks(callback)
 *         .execute();
 * }
 * }</pre>
 * 
 * <h3>Global Configuration:</h3>
 * <pre>{@code
 * // Configure default behaviors for your entire app
 * public class MyApplication extends Application {
 *     @Override
 *     public void onCreate() {
 *         super.onCreate();
 *         
 *         GrantlyConfig config = new GrantlyConfig.Builder()
 *             .setDefaultLazyMode(true)
 *             .setDefaultDenialBehavior(DenialBehavior.CONTINUE_APP_FLOW)
 *             .setLoggingEnabled(BuildConfig.DEBUG)
 *             .build();
 *             
 *         Grantly.configure(config);
 *     }
 * }
 * }</pre>
 * 
 * <h3>Special Permissions:</h3>
 * <pre>{@code
 * // Overlay permission (SYSTEM_ALERT_WINDOW)
 * Grantly.requestPermissions(this)
 *     .permissions(Manifest.permission.SYSTEM_ALERT_WINDOW)
 *     .setCallbacks(callback)
 *     .execute();
 *     
 * // Background location (Android 10+)
 * Grantly.requestPermissions(this)
 *     .permissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
 *     .setRationale("Background Location", 
 *         "Allow background location to track your runs even when the app is closed")
 *     .execute();
 * }</pre>
 * 
 * <p><strong>Thread Safety:</strong> This class is thread-safe and can be called from any thread.
 * However, UI-related operations will always be dispatched to the main thread.</p>
 * 
 * <p><strong>Lifecycle:</strong> The SDK automatically handles Activity lifecycle events and 
 * configuration changes. No manual cleanup is required in most cases.</p>
 * 
 * @since 1.0.0
 * @see PermissionRequest
 * @see GrantlyConfig
 * @see GrantlyCallback
 * @see GrantlyUtils
 */
public final class Grantly {
    
    private static final String TAG = "Grantly";
    
    private static volatile GrantlyConfig globalConfig = GrantlyConfig.getDefault();
    private static volatile boolean initialized = false;
    private static volatile PermissionManager permissionManager;
    private static volatile PermissionChecker permissionChecker;
    private static volatile ManifestParser manifestParser;
    private static volatile SpecialPermissionHandler specialPermissionHandler;
    private static volatile Context applicationContext;
    
    // Private constructor to prevent instantiation
    private Grantly() {
        throw new AssertionError("Grantly is a utility class and should not be instantiated");
    }
    
    /**
     * Create a new permission request builder for an Activity context.
     * 
     * @param activity The Activity context for the permission request
     * @return A new PermissionRequest builder instance
     * @throws IllegalArgumentException if activity is null
     */
    @NonNull
    public static PermissionRequest requestPermissions(@NonNull Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        
        ensureInitialized(activity.getApplicationContext());
        return new PermissionRequest(activity);
    }
    
    /**
     * Create a new permission request builder for a Fragment context.
     * 
     * @param fragment The Fragment context for the permission request
     * @return A new PermissionRequest builder instance
     * @throws IllegalArgumentException if fragment is null or fragment.getActivity() is null
     */
    @NonNull
    public static PermissionRequest requestPermissions(@NonNull Fragment fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("Fragment cannot be null");
        }
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("Fragment must be attached to an Activity");
        }
        
        ensureInitialized(fragment.getActivity().getApplicationContext());
        return new PermissionRequest(fragment);
    }
    
    /**
     * Configure the global settings for the Grantly SDK.
     * This configuration will be used as defaults for all permission requests
     * unless overridden at the request level.
     * 
     * @param config The global configuration to apply
     * @throws IllegalArgumentException if config is null
     */
    public static void configure(@NonNull GrantlyConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("GrantlyConfig cannot be null");
        }
        
        synchronized (Grantly.class) {
            globalConfig = config;
            
            // Initialize logging based on configuration
            GrantlyLogger.setLoggingEnabled(config.isLoggingEnabled());
            if (config.isLoggingEnabled()) {
                GrantlyLogger.i(TAG, "Grantly SDK configured with logging enabled");
                GrantlyLogger.logConfiguration(TAG, "GlobalConfig", 
                    "LazyMode=" + config.isDefaultLazyMode() + 
                    ", DenialBehavior=" + config.getDefaultDenialBehavior() +
                    ", CustomUI=" + (config.getCustomUiProvider() != null));
            }
            
            initialized = true;
        }
    }
    
    /**
     * Get the current global configuration for the Grantly SDK.
     * 
     * @return The current global GrantlyConfig instance
     */
    @NonNull
    public static GrantlyConfig getConfig() {
        return globalConfig;
    }
    
    /**
     * Initialize the SDK with default configuration if not already initialized.
     * This method is called automatically when needed and typically doesn't
     * need to be called manually.
     * 
     * @param context Application context for initialization
     */
    public static void initialize(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        ensureInitialized(context.getApplicationContext());
    }
    
    /**
     * Reset the SDK to its default state.
     * This method is primarily intended for testing purposes.
     * 
     * <p><strong>Warning:</strong> This will reset all global configuration
     * and should not be called in production code.</p>
     */
    public static void reset() {
        synchronized (Grantly.class) {
            globalConfig = GrantlyConfig.getDefault();
            initialized = false;
            permissionManager = null;
            permissionChecker = null;
            manifestParser = null;
            specialPermissionHandler = null;
            applicationContext = null;
        }
    }
    
    /**
     * Check if the SDK has been initialized.
     * 
     * @return true if the SDK is initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Cleanup any resources held by the SDK.
     * This method should be called when the application is shutting down
     * or when the SDK is no longer needed.
     * 
     * <p>After calling this method, the SDK will need to be reinitialized
     * before it can be used again.</p>
     */
    public static void cleanup() {
        synchronized (Grantly.class) {
            if (permissionManager != null) {
                permissionManager.cleanupExpiredRequests();
            }
            
            permissionManager = null;
            permissionChecker = null;
            manifestParser = null;
            specialPermissionHandler = null;
            applicationContext = null;
            initialized = false;
        }
    }
    
    /**
     * Gets the PermissionManager instance for internal use.
     * 
     * @return The PermissionManager instance
     * @throws GrantlyException if the SDK is not initialized
     */
    static PermissionManager getPermissionManager() {
        if (permissionManager == null) {
            // Try to initialize if we have an application context
            if (applicationContext != null) {
                ensureInitialized(applicationContext);
            }
            
            if (permissionManager == null) {
                throw new GrantlyException("Grantly SDK not initialized. Call Grantly.initialize() or use requestPermissions() first.");
            }
        }
        return permissionManager;
    }
    
    /**
     * Gets the PermissionChecker instance for internal use.
     * 
     * @return The PermissionChecker instance
     * @throws GrantlyException if the SDK is not initialized
     */
    static PermissionChecker getPermissionChecker() {
        if (permissionChecker == null) {
            throw new GrantlyException("Grantly SDK not initialized. Call Grantly.initialize() or use requestPermissions() first.");
        }
        return permissionChecker;
    }
    
    /**
     * Gets the ManifestParser instance for internal use.
     * 
     * @return The ManifestParser instance
     * @throws GrantlyException if the SDK is not initialized
     */
    static ManifestParser getManifestParser() {
        if (manifestParser == null) {
            throw new GrantlyException("Grantly SDK not initialized. Call Grantly.initialize() or use requestPermissions() first.");
        }
        return manifestParser;
    }
    
    /**
     * Gets the SpecialPermissionHandler instance for internal use.
     * 
     * @return The SpecialPermissionHandler instance
     * @throws GrantlyException if the SDK is not initialized
     */
    static SpecialPermissionHandler getSpecialPermissionHandler() {
        if (specialPermissionHandler == null) {
            throw new GrantlyException("Grantly SDK not initialized. Call Grantly.initialize() or use requestPermissions() first.");
        }
        return specialPermissionHandler;
    }
    
    /**
     * Handles permission results from Activity.onRequestPermissionsResult().
     * This method should be called from the host Activity's onRequestPermissionsResult method.
     * 
     * @param requestCode The request code passed to requestPermissions
     * @param permissions The requested permissions
     * @param grantResults The grant results for the corresponding permissions
     * @return true if the result was handled by Grantly, false otherwise
     */
    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionManager != null) {
            return permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        return false;
    }
    
    /**
     * Ensure the SDK is initialized with the given context.
     * 
     * @param context Application context for initialization
     */
    private static void ensureInitialized(@NonNull Context context) {
        if (!initialized) {
            synchronized (Grantly.class) {
                if (!initialized) {
                    try {
                        // Store application context
                        applicationContext = context.getApplicationContext();
                        
                        // Initialize with default configuration if not already set
                        if (globalConfig == null) {
                            globalConfig = GrantlyConfig.getDefault();
                        }
                        
                        // Initialize logging based on configuration
                        GrantlyLogger.setLoggingEnabled(globalConfig.isLoggingEnabled());
                        
                        GrantlyLogger.d(TAG, "Initializing Grantly SDK");
                        
                        // Initialize core components
                        manifestParser = new ManifestParser();
                        permissionChecker = new PermissionChecker(applicationContext);
                        specialPermissionHandler = new SpecialPermissionHandler();
                        permissionManager = new PermissionManager(permissionChecker);
                        
                        // Initialize default UI providers if custom ones are not set
                        initializeDefaultProviders();
                        
                        initialized = true;
                        
                        GrantlyLogger.i(TAG, "Grantly SDK initialized successfully");
                        
                    } catch (Exception e) {
                        GrantlyLogger.e(TAG, "Failed to initialize Grantly SDK", e);
                        throw new GrantlyException("Failed to initialize Grantly SDK", e);
                    }
                }
            }
        }
    }
    
    /**
     * Initialize default UI providers if custom ones are not configured.
     */
    private static void initializeDefaultProviders() {
        if (globalConfig.getCustomUiProvider() == null) {
            // Default providers will be created on-demand
            // This avoids circular dependencies during initialization
        }
    }
}