package es.joshluq.authkit.showcase

import android.app.Application
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.authkit.session.sdk.SessionKitConfig
import java.util.concurrent.TimeUnit

class ShowcaseApp : Application() {

    lateinit var authKit: AuthKit
        private set

    override fun onCreate() {
        super.onCreate()
        initializeAuthKit()
    }

    private fun initializeAuthKit() {
        authKit = AuthKit.init(this) {
            storeName = "showcase_auth_store"
            
            addFeature(SessionKit, SessionKitConfig.build {
                persistence = PersistencePolicy.Persistent
                expiration = ExpirationPolicy.Timed(
                    durationMillis = TimeUnit.SECONDS.toMillis(30),
                    warningThresholdMillis = TimeUnit.SECONDS.toMillis(10),
                )
                interactions = InteractionPolicy.Timed(
                    throttleIntervalMillis = TimeUnit.SECONDS.toMillis(2)
                )
            })
        }
    }
}
