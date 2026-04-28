package es.joshluq.authkit.session.sdk

import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.foundationkit.manager.ManagerConfig

class SessionKitConfig(
    val persistence: PersistencePolicy = PersistencePolicy.Persistent,
    val expiration: ExpirationPolicy = ExpirationPolicy.Never
) : ManagerConfig {

    class Builder {
        var persistence: PersistencePolicy = PersistencePolicy.Persistent
        var expiration: ExpirationPolicy = ExpirationPolicy.Never

        fun build(): SessionKitConfig = SessionKitConfig(
            persistence = persistence,
            expiration = expiration
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
