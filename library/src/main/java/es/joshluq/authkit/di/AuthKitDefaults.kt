package es.joshluq.authkit.di

import es.joshluq.foundationkit.log.LoggerDefaults
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.provider.SerializerProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Default implementations and constants for Authkit.
 * Strictly follows the 'Defaults' pattern for internal configuration.
 */
internal object AuthKitDefaults {

    private const val TAG = "Authkit"

    val logger: LoggerKit by lazy {
        LoggerKit.Builder()
            .setProvider(LoggerDefaults.defaultLogProvider(tagPrefix = TAG, showThread = false))
            .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val defaultSerializer: SerializerProvider by lazy {
        object : SerializerProvider {
            override fun <T : Any> serialize(value: T, type: Class<T>): String {
                val serializer = json.serializersModule.serializer(type)
                return json.encodeToString(serializer, value)
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any> deserialize(value: String, type: Class<T>): T {
                val serializer = json.serializersModule.serializer(type)
                return json.decodeFromString(serializer, value) as T
            }
        }
    }
}
