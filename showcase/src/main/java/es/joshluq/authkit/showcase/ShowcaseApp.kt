package es.joshluq.authkit.showcase

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.authkit.session.sdk.SessionKitConfig
import java.util.concurrent.TimeUnit

class ShowcaseApp : Application(), Configuration.Provider {

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

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return if (::authKit.isInitialized) {
                        authKit.session.workerFactory()
                            .createWorker(appContext, workerClassName, workerParameters)
                    } else {
                        null
                    }
                }
            })
            .build()
}
