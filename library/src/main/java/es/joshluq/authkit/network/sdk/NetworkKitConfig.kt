package es.joshluq.authkit.network.sdk

import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for the NetworkKit plugin.
 */
class NetworkKitConfig internal constructor(
    val tokenRefresher: TokenRefresher? = null,
    val sessionProvider: NetworkSessionProvider? = null
) : ManagerConfig {

    class Builder {
        var tokenRefresher: TokenRefresher? = null
        var sessionProvider: NetworkSessionProvider? = null

        fun build(): NetworkKitConfig = NetworkKitConfig(
            tokenRefresher = tokenRefresher,
            sessionProvider = sessionProvider
        )
    }

    companion object {
        inline fun build(block: Builder.() -> Unit): NetworkKitConfig =
            Builder().apply(block).build()
    }
}
