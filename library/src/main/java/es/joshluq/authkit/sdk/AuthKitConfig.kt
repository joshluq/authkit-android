package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.authkit.di.AuthKitDefaults
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.ManagerConfig
import es.joshluq.foundationkit.provider.SerializerProvider

data class AuthKitConfig(
    val context: Context,
    val sessionConfig: SessionKitConfig,
    val storeName: String,
    val logger: Loggerkit
    ) : ManagerConfig {

    /**
     * Builder class for [AuthKitConfig].
     */
    class Builder {

        var context: Context? = null
        var sessionConfig : SessionKitConfig? = null
        var storeName: String = "auth_kit_store"
        var logger: Loggerkit = AuthKitDefaults.logger

        fun build() = AuthKitConfig(
            context = checkNotNull(context) { "Context is required for AuthKit initialization." },
            sessionConfig = checkNotNull(sessionConfig) { "SessionKitConfig is required for AuthKit initialization." },
            storeName = storeName,
            logger = logger,
        )

        companion object {
            inline fun build(context: Context, sessionKitConfig: SessionKitConfig, block: Builder.() -> Unit): AuthKitConfig {
                return Builder().apply {
                    this.context = context
                    this.sessionConfig = sessionKitConfig
                }.apply(block).build()
            }
        }

    }
}
