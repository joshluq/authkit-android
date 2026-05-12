package es.joshluq.authkit.session.sdk

import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for [SessionKit].
 */
class SessionKitConfig internal constructor(
    val persistence: PersistencePolicy = PersistencePolicy.Persistent,
    val expiration: ExpirationPolicy = ExpirationPolicy.Never,
    val interactions: InteractionPolicy = InteractionPolicy.None
) : ManagerConfig {

    /**
     * Builder class for [SessionKitConfig].
     */
    class Builder {
        var persistence: PersistencePolicy = PersistencePolicy.Persistent
        var expiration: ExpirationPolicy = ExpirationPolicy.Never
        var interactions: InteractionPolicy = InteractionPolicy.None

        fun build(): SessionKitConfig = SessionKitConfig(
            persistence = persistence,
            expiration = expiration,
            interactions = interactions
        )
    }

    companion object {
        /**
         * DSL entry point for creating a [SessionKitConfig] instance.
         */
        inline fun build(block: Builder.() -> Unit): SessionKitConfig =
            Builder().apply(block).build()
    }
}
