package es.joshluq.authkit.di

import es.joshluq.authkit.sdk.AuthKitConfig
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

    val context by lazy { config.context }

    val logger: Loggerkit by lazy { config.logger }

    val transientStorage: StorageProvider by lazy {
        CacheStorageProvider()
    }

    val serializer: SerializerProvider by lazy { AuthKitDefaults.defaultSerializer }

    // Storage
    val persistentStorage: StorageProvider by lazy {
        val sharedPrefs = config.context.getSharedPreferences(config.storeName, android.content.Context.MODE_PRIVATE)
        SharedPreferencesStorageProvider(sharedPrefs, serializer)
    }
}
