package es.joshluq.authkit.di

import androidx.work.WorkManager
import es.joshluq.authkit.sdk.AuthKitConfig
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.SerializerProvider
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.storage.CacheStorageProvider
import es.joshluq.foundationkit.storage.SharedPreferencesStorageProvider

/**
 * Internal Service Locator for AuthKit SDK.
 * Manages the manual instantiation of all internal components.
 */
internal class AuthKitComponent(
    private val config: AuthKitConfig
) {

    val logger: Loggerkit by lazy { config.logger }

    val workManager: WorkManager by lazy {
        WorkManager.getInstance(config.context)
    }

    val transientStorage: StorageProvider by lazy {
        CacheStorageProvider()
    }

    val serializer: SerializerProvider by lazy { AuthKitDefaults.defaultSerializer }

    // Storage
    val persistentStorage: StorageProvider by lazy {
        val sharedPrefs = config.context.getSharedPreferences(config.storeName, android.content.Context.MODE_PRIVATE)
        SharedPreferencesStorageProvider(sharedPrefs, serializer)
    }

    val session: SessionKit by lazy {
        SessionKit.Builder().build(config.sessionConfig).apply {
            initialize(persistentStorage, transientStorage, workManager, logger)
        }
    }
}
