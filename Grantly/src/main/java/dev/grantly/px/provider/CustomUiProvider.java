package dev.grantly.px.provider;

/**
 * Interface for providing custom UI providers for permission requests.
 * This allows developers to customize all aspects of the permission UI.
 */
public interface CustomUiProvider {
    
    /**
     * Get the custom dialog provider for permission requests.
     * 
     * @return DialogProvider instance, or null to use default
     */
    DialogProvider getDialogProvider();
    
    /**
     * Get the custom toast provider for permission feedback.
     * 
     * @return ToastProvider instance, or null to use default
     */
    ToastProvider getToastProvider();
    
    /**
     * Get the custom rationale provider for permission explanations.
     * 
     * @return RationaleProvider instance, or null to use default
     */
    RationaleProvider getRationaleProvider();
}