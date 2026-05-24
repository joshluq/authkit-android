package es.joshluq.authkit.network.sdk

import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for the NetworkKit plugin.
 * Defines the parameters needed for network automation capabilities,
 * such as silent token refresh mechanisms and session provisioning.
 *
 * @property tokenRefresher An optional strategy to refresh authentication tokens.
 * @property sessionProvider An optional provider to manage session-related network operations.
 */
class NetworkKitConfig internal constructor(
    val tokenRefresher: TokenRefresher? = null,
    val sessionProvider: NetworkSessionProvider? = null
) : ManagerConfig {

    /**
     * Builder class for creating instances of [NetworkKitConfig].
     */
    class Builder {
        /** The token refresher strategy. */
        var tokenRefresher: TokenRefresher? = null

        /** The network session provider strategy. */
        var sessionProvider: NetworkSessionProvider? = null

        /**
         * Builds the [NetworkKitConfig] instance.
         *
         * @return The configured [NetworkKitConfig].
         */
        fun build(): NetworkKitConfig = NetworkKitConfig(
            tokenRefresher = tokenRefresher,
            sessionProvider = sessionProvider
        )
    }

    companion object {
        /**
         * Inline builder block function to create a [NetworkKitConfig].
         *
         * @param block The configuration block applied to the builder.
         * @return The configured [NetworkKitConfig].
         */
        inline fun build(block: Builder.() -> Unit): NetworkKitConfig =
            Builder().apply(block).build()
    }
}
