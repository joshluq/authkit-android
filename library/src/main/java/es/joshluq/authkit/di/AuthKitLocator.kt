package es.joshluq.authkit.di

import android.annotation.SuppressLint
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.sdk.SessionKit

/**
 * Internal Service Locator for AuthKit SDK.
 * Manages the global instance of the SDK and provides access to its internal components.
 */
internal object AuthKitLocator {
    @SuppressLint("StaticFieldLeak")
    private var authKit: AuthKit? = null

    /**
     * Registers the global AuthKit instance.
     */
    fun register(instance: AuthKit) {
        authKit = instance
    }

    /**
     * Retrieves the global AuthKit instance.
     * @throws IllegalStateException if AuthKit has not been initialized.
     */
    fun getAuthKit(): AuthKit = authKit ?: error("AuthKit is not initialized. Call AuthKit.init(context) first.")

    /**
     * Helper method to resolve the SessionKit plugin.
     */
    fun resolveSessionKit(): SessionKit = getAuthKit().plugin<SessionKit>() ?: error("SessionKit plugin not installed")
}
