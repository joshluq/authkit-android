package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.authkit.di.AuthKitComponent
import es.joshluq.authkit.di.AuthKitDefaults
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.Manager

/**
 * Main entry point for AuthKit SDK.
 * Acts as a container for plugins and provides centralized access to their instances.
 */
class AuthKit private constructor(
    val context: Context,
    val logger: Loggerkit,
    val storeName: String
) : Manager<AuthKitConfig>() {

    internal val component: AuthKitComponent by lazy {
        AuthKitComponent(AuthKitConfig(context, storeName, logger))
    }

    @PublishedApi
    internal val plugins = mutableMapOf<Class<*>, Any>()

    /**
     * Access to the session plugin instance.
     */
    val session: SessionKit
        get() = plugin<SessionKit>() ?: error("Session plugin not installed")

    /**
     * Returns the instance of the requested plugin type if it is installed.
     */
    inline fun <reified T : Any> plugin(): T? {
        return plugins[T::class.java] as? T
    }

    /**
     * Internal method to register a plugin instance.
     */
    internal fun registerPlugin(pluginClass: Class<*>, instance: Any) {
        plugins[pluginClass] = instance
    }

    /**
     * DSL Builder for [AuthKit] initialization.
     */
    class Builder(private val context: Context) {
        var logger: Loggerkit = AuthKitDefaults.logger
        var storeName: String = "auth_kit_store"
        private val installers = mutableListOf<(AuthKit) -> Unit>()

        /**
         * Adds a feature to the AuthKit instance.
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

        fun build(): AuthKit {
            val authKit = AuthKit(context, logger, storeName)
            installers.forEach { it(authKit) }
            return authKit
        }
    }

    companion object {
        /**
         * Initializes AuthKit using a DSL block.
         */
        inline fun init(context: Context, block: Builder.() -> Unit): AuthKit {
            return Builder(context).apply(block).build()
        }
    }
}
