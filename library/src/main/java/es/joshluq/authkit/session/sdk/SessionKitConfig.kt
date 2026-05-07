package es.joshluq.authkit.session.sdk

import es.joshluq.authkit.di.SessionKitDefaults
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.foundationkit.manager.ManagerConfig

class SessionKitConfig internal constructor(
    val persistence: PersistencePolicy,
    val expiration: ExpirationPolicy,
    val interactions: InteractionConfig
) : ManagerConfig {

    class Builder {
        var persistence: PersistencePolicy = PersistencePolicy.Persistent
        var expiration: ExpirationPolicy = ExpirationPolicy.Never
        var interactions: InteractionConfig = SessionKitDefaults.interactionConfig()

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
