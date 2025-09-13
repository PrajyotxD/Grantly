package dev.grantly.px.provider.impl;

import dev.grantly.px.provider.CustomUiProvider;
import dev.grantly.px.provider.DialogProvider;
import dev.grantly.px.provider.RationaleProvider;
import dev.grantly.px.provider.ToastProvider;

/**
 * Default implementation of CustomUiProvider that provides all default UI components.
 * This can be used as a base class for custom implementations or used directly.
 */
public class DefaultCustomUiProvider implements CustomUiProvider {
    
    private final DialogProvider dialogProvider;
    private final ToastProvider toastProvider;
    private final RationaleProvider rationaleProvider;
    
    /**
     * Creates a DefaultCustomUiProvider with all default implementations.
     */
    public DefaultCustomUiProvider() {
        this(new DefaultDialogProvider(), new DefaultToastProvider(), new DefaultRationaleProvider());
    }
    
    /**
     * Creates a DefaultCustomUiProvider with custom providers.
     * 
     * @param dialogProvider Custom dialog provider, or null for default
     * @param toastProvider Custom toast provider, or null for default
     * @param rationaleProvider Custom rationale provider, or null for default
     */
    public DefaultCustomUiProvider(DialogProvider dialogProvider, ToastProvider toastProvider, 
                                 RationaleProvider rationaleProvider) {
        this.dialogProvider = dialogProvider != null ? dialogProvider : new DefaultDialogProvider();
        this.toastProvider = toastProvider != null ? toastProvider : new DefaultToastProvider();
        this.rationaleProvider = rationaleProvider != null ? rationaleProvider : new DefaultRationaleProvider();
    }
    
    @Override
    public DialogProvider getDialogProvider() {
        return dialogProvider;
    }
    
    @Override
    public ToastProvider getToastProvider() {
        return toastProvider;
    }
    
    @Override
    public RationaleProvider getRationaleProvider() {
        return rationaleProvider;
    }
}