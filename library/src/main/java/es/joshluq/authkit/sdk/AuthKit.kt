package es.joshluq.authkit.sdk

import es.joshluq.authkit.di.AuthKitComponent
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.foundationkit.manager.Manager
import es.joshluq.foundationkit.manager.ManagerBuilder

class AuthKit internal constructor(
    private val componentFactory: (AuthKitConfig) -> AuthKitComponent = { AuthKitComponent(it) }
) : Manager<AuthKitConfig>() {

    companion object {
        private const val TAG = "AuthKit"
    }

    val session: SessionKit get() = component.session

    internal lateinit var component: AuthKitComponent

    private fun initialize(config: AuthKitConfig) {
        this.config = config
        this.component = componentFactory(config)
        component.logger.i(TAG, "AuthKit initialized successfully.")
    }

    class Builder : ManagerBuilder<AuthKitConfig> {

        /**
         * Builds and initializes a new instance of [AuthKit].
         *
         * @param config The [AuthKitConfig] required to configure the SDK.
         * @return A fully initialized [AuthKit] instance.
         */
        override fun build(config: AuthKitConfig): AuthKit {
            return AuthKit().apply {
                initialize(config)
            }
        }
    }
}
