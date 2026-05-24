package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.authkit.di.AuthKitComponent
import es.joshluq.authkit.di.AuthKitDefaults
import es.joshluq.authkit.di.AuthKitLocator
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.manager.Manager

/**
 * Main entry point for the AuthKit SDK.
 * Acts as a container for all authentication-related plugins and provides
 * centralized access to their instances.
 */
class AuthKit private constructor(
    context: Context,
    private val storeName: String,
    private val encryptionAlias: String,
    private val logger: LoggerKit
) : Manager<AuthKitConfig>() {

    val context: Context = context.applicationContext

    companion object {
        /**
         * Initializes the [AuthKit] SDK using a DSL configuration block.
         *
         * @param context The application context to be used by the SDK.
         * @param block A configuration block applied to the [Builder].
         * @return The configured and initialized [AuthKit] instance.
         */
        @JvmStatic
        fun init(context: Context, block: Builder.() -> Unit): AuthKit {
            return Builder(context).apply(block).build().also {
                AuthKitLocator.register(it)
            }
        }
    }

    internal val component: AuthKitComponent by lazy {
        AuthKitComponent(AuthKitConfig(this.context, storeName, encryptionAlias, logger))
    }

    @PublishedApi
    internal val plugins = mutableMapOf<Class<*>, Any>()

    /**
     * Access to the session plugin instance.
     * Throws an exception if the SessionKit plugin has not been installed.
     */
    val session: SessionKit
        get() = plugin<SessionKit>() ?: error("Session plugin not installed")

    /**
     * Returns the instance of the requested plugin type if it is installed.
     *
     * @param T The type of the plugin to retrieve.
     * @return The plugin instance of type [T], or null if it's not registered.
     */
    inline fun <reified T : Any> plugin(): T? {
        return plugins[T::class.java] as? T
    }

    /**
     * Internal method to register a plugin instance.
     *
     * @param pluginClass The class of the plugin being registered.
     * @param instance The plugin instance to associate with the class.
     */
    internal fun registerPlugin(pluginClass: Class<*>, instance: Any) {
        plugins[pluginClass] = instance
    }

    /**
     * DSL Builder for [AuthKit] initialization.
     * Allows configuring core settings and adding features (plugins).
     *
     * @property context The application context.
     */
    class Builder(private val context: Context) {
        var storeName: String = "auth_kit_store"
        var encryptionAlias: String = "AUTHKIT_DEFAULT_ALIAS"
        var logger: LoggerKit = AuthKitDefaults.logger
        private val installers = mutableListOf<(AuthKit) -> Unit>()

        /**
         * Adds a feature (plugin) to the [AuthKit] instance.
         *
         * @param TConfig The type of the configuration for the plugin.
         * @param TInstance The type of the plugin instance.
         * @param plugin The plugin definition to be installed.
         * @param config The configuration to initialize the plugin.
         */
        fun <TConfig : Any, TInstance : Any> addFeature(
            plugin: AuthKitPlugin<TConfig, TInstance>,
            config: TConfig
        ) {
            installers.add { authKit ->
                val instance = plugin.install(authKit, config)
                authKit.registerPlugin(instance::class.java, instance)
            }
        }

        /**
         * Builds the final [AuthKit] instance and applies all registered plugin installers.
         *
         * @return A fully initialized [AuthKit].
         */
        fun build(): AuthKit {
            val authKit = AuthKit(context, storeName, encryptionAlias, logger)
            installers.forEach { it(authKit) }
            return authKit
        }
    }
}
