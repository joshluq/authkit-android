package es.joshluq.authkit.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import es.joshluq.authkit.sdk.AuthKitConfig
import es.joshluq.encryptionkit.sdk.EncryptionKit
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.provider.SerializerProvider
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.storage.CacheStorageProvider

/**
 * Internal Service Locator for AuthKit SDK.
 * Manages the manual instantiation of all internal components.
 */
internal class AuthKitComponent(
    private val config: AuthKitConfig
) {

    val context by lazy { config.context }

    val logger: LoggerKit by lazy { config.logger }

    val transientStorage: StorageProvider by lazy {
        CacheStorageProvider()
    }

    val serializer: SerializerProvider by lazy { AuthKitDefaults.defaultSerializer }

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = config.storeName)

    val encryptionKit: EncryptionKit by lazy {
        EncryptionKit.build(context) {
            alias = config.encryptionAlias
            logger = this@AuthKitComponent.logger
        }
    }

    val persistentStorage: StorageProvider by lazy {
        encryptionKit.createSecureStorage(
            dataStore = context.dataStore,
            serializerProvider = serializer
        )
    }
}
