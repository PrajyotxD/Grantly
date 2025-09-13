package dev.grantly.px.config;

import androidx.annotation.StyleRes;
import dev.grantly.px.provider.CustomUiProvider;

/**
 * Configuration class for the Grantly SDK that allows developers to set global defaults
 * and customize the behavior of permission requests throughout their application.
 * 
 * <p>This class provides a centralized way to configure default behaviors, UI themes,
 * logging settings, and custom providers that will be used across all permission requests
 * unless overridden at the individual request level.</p>
 * 
 * <h3>Basic Configuration:</h3>
 * <pre>{@code
 * public class MyApplication extends Application {
 *     @Override
 *     public void onCreate() {
 *         super.onCreate();
 *         
 *         GrantlyConfig config = GrantlyConfig.builder()
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
 * <h3>Advanced Configuration with Custom UI:</h3>
 * <pre>{@code
 * GrantlyConfig config = GrantlyConfig.builder()
 *     .setDefaultLazyMode(true)
 *     .setDefaultDenialBehavior(DenialBehavior.DISABLE_FEATURE)
 *     .setCustomUiProvider(new MyCustomUiProvider())
 *     .setDefaultDialogTheme(R.style.MyPermissionDialogTheme)
 *     .setDefaultToastTheme(R.style.MyToastTheme)
 *     .setDefaultRationaleTitle("Permission Needed")
 *     .setDefaultRationaleMessage("This app needs permissions to provide you with the best experience")
 *     .setLoggingEnabled(BuildConfig.DEBUG)
 *     .build();
 * }</pre>
 * 
 * <h3>Configuration Options:</h3>
 * <ul>
 *   <li><strong>Lazy Mode:</strong> Whether permissions should be requested contextually or upfront</li>
 *   <li><strong>Denial Behavior:</strong> How the app should respond when permissions are denied</li>
 *   <li><strong>Custom UI Provider:</strong> Custom implementations for dialogs, toasts, and rationales</li>
 *   <li><strong>Themes:</strong> Default styling for UI components</li>
 *   <li><strong>Logging:</strong> Enable/disable SDK logging for debugging</li>
 *   <li><strong>Default Rationale:</strong> Default title and message for permission explanations</li>
 * </ul>
 * 
 * <p><strong>Thread Safety:</strong> This class is immutable once built and is thread-safe.
 * The Builder class is not thread-safe and should be used from a single thread.</p>
 * 
 * <p><strong>Performance:</strong> Configuration is cached globally, so there's no performance
 * penalty for accessing configuration values frequently.</p>
 * 
 * @since 1.0.0
 * @see Grantly#configure(GrantlyConfig)
 * @see DenialBehavior
 * @see CustomUiProvider
 */
public class GrantlyConfig {
    private final boolean defaultLazyMode;
    private final DenialBehavior defaultDenialBehavior;
    private final CustomUiProvider customUiProvider;
    private final boolean enableLogging;
    private final int defaultDialogTheme;
    private final int defaultToastTheme;
    private final boolean showRationaleByDefault;
    private final String defaultRationaleTitle;
    private final String defaultRationaleMessage;

    private GrantlyConfig(Builder builder) {
        this.defaultLazyMode = builder.defaultLazyMode;
        this.defaultDenialBehavior = builder.defaultDenialBehavior;
        this.customUiProvider = builder.customUiProvider;
        this.enableLogging = builder.enableLogging;
        this.defaultDialogTheme = builder.defaultDialogTheme;
        this.defaultToastTheme = builder.defaultToastTheme;
        this.showRationaleByDefault = builder.showRationaleByDefault;
        this.defaultRationaleTitle = builder.defaultRationaleTitle;
        this.defaultRationaleMessage = builder.defaultRationaleMessage;
    }

    // Getters
    public boolean isDefaultLazyMode() {
        return defaultLazyMode;
    }

    public DenialBehavior getDefaultDenialBehavior() {
        return defaultDenialBehavior;
    }

    public CustomUiProvider getCustomUiProvider() {
        return customUiProvider;
    }

    public boolean isLoggingEnabled() {
        return enableLogging;
    }

    public int getDefaultDialogTheme() {
        return defaultDialogTheme;
    }

    public int getDefaultToastTheme() {
        return defaultToastTheme;
    }

    public boolean shouldShowRationaleByDefault() {
        return showRationaleByDefault;
    }

    public String getDefaultRationaleTitle() {
        return defaultRationaleTitle;
    }

    public String getDefaultRationaleMessage() {
        return defaultRationaleMessage;
    }

    /**
     * Builder class for creating GrantlyConfig instances with validation and default values.
     * 
     * <p>This builder provides a fluent API for configuring all aspects of the Grantly SDK.
     * All methods return the builder instance to allow method chaining. The {@link #build()}
     * method validates the configuration and creates an immutable GrantlyConfig instance.</p>
     * 
     * <p>Example usage:</p>
     * <pre>{@code
     * GrantlyConfig config = GrantlyConfig.builder()
     *     .setDefaultLazyMode(true)
     *     .setLoggingEnabled(BuildConfig.DEBUG)
     *     .setDefaultDenialBehavior(DenialBehavior.CONTINUE_APP_FLOW)
     *     .build();
     * }</pre>
     * 
     * @since 1.0.0
     */
    public static class Builder {
        private boolean defaultLazyMode = false;
        private DenialBehavior defaultDenialBehavior = DenialBehavior.CONTINUE_APP_FLOW;
        private CustomUiProvider customUiProvider = null;
        private boolean enableLogging = false;
        private int defaultDialogTheme = 0;
        private int defaultToastTheme = 0;
        private boolean showRationaleByDefault = true;
        private String defaultRationaleTitle = "Permission Required";
        private String defaultRationaleMessage = "This permission is needed for the app to function properly.";

        /**
         * Set whether permission requests should use lazy mode by default.
         * In lazy mode, permissions are only requested when the feature is actually used.
         *
         * @param lazy true to enable lazy mode by default, false otherwise
         * @return this Builder instance for method chaining
         */
        public Builder setDefaultLazyMode(boolean lazy) {
            this.defaultLazyMode = lazy;
            return this;
        }

        /**
         * Set the default behavior when permissions are denied.
         *
         * @param behavior the denial behavior to use by default
         * @return this Builder instance for method chaining
         * @throws IllegalArgumentException if behavior is null
         */
        public Builder setDefaultDenialBehavior(DenialBehavior behavior) {
            if (behavior == null) {
                throw new IllegalArgumentException("DenialBehavior cannot be null");
            }
            this.defaultDenialBehavior = behavior;
            return this;
        }

        /**
         * Set a custom UI provider for dialogs, toasts, and rationale displays.
         *
         * @param provider the custom UI provider, or null to use default implementations
         * @return this Builder instance for method chaining
         */
        public Builder setCustomUiProvider(CustomUiProvider provider) {
            this.customUiProvider = provider;
            return this;
        }

        /**
         * Enable or disable logging throughout the SDK.
         *
         * @param enabled true to enable logging, false to disable
         * @return this Builder instance for method chaining
         */
        public Builder setLoggingEnabled(boolean enabled) {
            this.enableLogging = enabled;
            return this;
        }

        /**
         * Set the default theme for permission dialogs.
         *
         * @param theme the style resource ID for the dialog theme
         * @return this Builder instance for method chaining
         */
        public Builder setDefaultDialogTheme(@StyleRes int theme) {
            this.defaultDialogTheme = theme;
            return this;
        }

        /**
         * Set the default theme for toast messages.
         *
         * @param theme the style resource ID for the toast theme
         * @return this Builder instance for method chaining
         */
        public Builder setDefaultToastTheme(@StyleRes int theme) {
            this.defaultToastTheme = theme;
            return this;
        }

        /**
         * Set whether rationale should be shown by default when permissions are denied.
         *
         * @param showRationale true to show rationale by default, false otherwise
         * @return this Builder instance for method chaining
         */
        public Builder setShowRationaleByDefault(boolean showRationale) {
            this.showRationaleByDefault = showRationale;
            return this;
        }

        /**
         * Set the default title for rationale dialogs.
         *
         * @param title the default title text
         * @return this Builder instance for method chaining
         * @throws IllegalArgumentException if title is null or empty
         */
        public Builder setDefaultRationaleTitle(String title) {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalArgumentException("Rationale title cannot be null or empty");
            }
            this.defaultRationaleTitle = title;
            return this;
        }

        /**
         * Set the default message for rationale dialogs.
         *
         * @param message the default message text
         * @return this Builder instance for method chaining
         * @throws IllegalArgumentException if message is null or empty
         */
        public Builder setDefaultRationaleMessage(String message) {
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("Rationale message cannot be null or empty");
            }
            this.defaultRationaleMessage = message;
            return this;
        }

        /**
         * Build the GrantlyConfig instance with the configured values.
         *
         * @return a new GrantlyConfig instance
         */
        public GrantlyConfig build() {
            validateConfiguration();
            return new GrantlyConfig(this);
        }

        /**
         * Validate the configuration before building.
         *
         * @throws IllegalStateException if the configuration is invalid
         */
        private void validateConfiguration() {
            // Validate that if custom UI provider is set, it's properly configured
            if (customUiProvider != null) {
                // Additional validation could be added here if needed
            }

            // Validate theme resources are valid (non-negative)
            if (defaultDialogTheme < 0) {
                throw new IllegalStateException("Default dialog theme must be a valid resource ID");
            }
            if (defaultToastTheme < 0) {
                throw new IllegalStateException("Default toast theme must be a valid resource ID");
            }
        }
    }

    /**
     * Create a new Builder instance for configuring GrantlyConfig.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a default GrantlyConfig instance with standard settings.
     *
     * @return a GrantlyConfig with default values
     */
    public static GrantlyConfig getDefault() {
        return new Builder().build();
    }
}