package ir.aispeaking.sharedui.ui.navigation

/**
 * Cross-platform back navigation coordinator for Android and Web (Wasm/JS).
 * Allows active screens (such as ChatScreen) to register a back event interceptor
 * so that hardware back gestures or browser history popstate events can trigger
 * finish/confirmation flows rather than immediately exiting the screen.
 */
object BackNavigationManager {
    private var interceptor: (() -> Boolean)? = null

    /**
     * Registers a back interceptor callback.
     * Return `true` if the event was consumed, or `false` to permit normal navigation.
     */
    fun registerInterceptor(handler: () -> Boolean) {
        interceptor = handler
    }

    /**
     * Unregisters the given interceptor if it is the currently active one.
     */
    fun unregisterInterceptor(handler: () -> Boolean) {
        if (interceptor == handler) {
            interceptor = null
        }
    }

    /**
     * Checks if an interceptor is currently registered.
     */
    fun hasInterceptor(): Boolean = interceptor != null

    /**
     * Invokes the active back interceptor if present.
     * Returns `true` if consumed, `false` otherwise.
     */
    fun handleBack(): Boolean {
        return interceptor?.invoke() ?: false
    }
}
