package es.joshluq.authkit.di

import androidx.work.WorkManager
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.StorageProvider

internal typealias ComponentFactory = (SessionKitConfig, StorageProvider, StorageProvider, WorkManager, Loggerkit) -> SessionKitComponent

object SessionKitDefaults {

    internal val factory =
        { session: SessionKitConfig, persistentStorage: StorageProvider, transientStorage: StorageProvider, workManager: WorkManager, logger: Loggerkit ->
            SessionKitComponent(
                config = session,
                persistentStorage = persistentStorage,
                transientStorage = transientStorage,
                workManager = workManager,
                logger = logger
            )
        }

    fun expirationConfig(
        durationMillis: Long = 5 * 60 * 1000,
        warningThresholdMillis: Long? = null
    ) = ExpirationPolicy.Timed(durationMillis, warningThresholdMillis)
}