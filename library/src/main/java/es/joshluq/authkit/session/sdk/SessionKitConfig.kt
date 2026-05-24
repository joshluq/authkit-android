package es.joshluq.authkit.session.sdk

import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for [SessionKit].
 * Defines the parameters needed to configure session lifecycle behaviors,
 * including how sessions are persisted, how they expire, and how user
 * interactions affect the session lifetime.
 *
 * @property persistence Policy indicating how session data
 * should be stored (e.g., persistent across app restarts or transient in memory).
 * @property expiration Policy indicating how and when the session
 * should expire (e.g., timed or never).
 * @property interactions Policy indicating what types of user
 * interactions can extend the session.
 */
class SessionKitConfig internal constructor(
    val persistence: PersistencePolicy = PersistencePolicy.Persistent,
    val expiration: ExpirationPolicy = ExpirationPolicy.Never,
    val interactions: InteractionPolicy = InteractionPolicy.None
) : ManagerConfig {

    /**
     * Builder class for creating instances of [SessionKitConfig].
     */
    class Builder {
        var persistence: PersistencePolicy = PersistencePolicy.Persistent

        var expiration: ExpirationPolicy = ExpirationPolicy.Never

        var interactions: InteractionPolicy = InteractionPolicy.None

        /**
         * Builds the [SessionKitConfig] instance.
         *
         * @return The configured [SessionKitConfig].
         */
        fun build(): SessionKitConfig = SessionKitConfig(
            persistence = persistence,
            expiration = expiration,
            interactions = interactions
        )
    }

    companion object {
        /**
         * DSL entry point for creating a [SessionKitConfig] instance.
         *
         * @param block The configuration block applied to the builder.
         * @return The configured [SessionKitConfig].
         */
        inline fun build(block: Builder.() -> Unit): SessionKitConfig =
            Builder().apply(block).build()
    }
}
