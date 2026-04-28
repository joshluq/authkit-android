package es.joshluq.authkit.di

import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionkitConfig
import es.joshluq.encryptionkit.sdk.EncryptionkitManager
import es.joshluq.foundationkit.log.LoggerDefaults
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.EncryptionProvider
import es.joshluq.foundationkit.provider.SerializerProvider
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Default implementations and constants for Authkit.
 * Strictly follows the 'Defaults' pattern for internal configuration.
 */
internal object AuthKitDefaults {

    private const val TAG = "Authkit"
    private const val DELIMITER = ":"
    private const val PARTS = 2
    private const val RADIX = 16

    val logger: Loggerkit by lazy {
        Loggerkit.Builder()
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
                val data = json.encodeToString(serializer, value)
                return defaultEncrypter.encrypt(data)
            }

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any> deserialize(value: String, type: Class<T>): T {
                val serializer = json.serializersModule.serializer(type)
                val decryptedData = defaultEncrypter.decrypt(value)
                return json.decodeFromString(serializer, decryptedData) as T
            }
        }
    }

    private val encryptionConfig = EncryptionkitConfig.build {
        alias = "DEFAULT_ALIAS"
        useStrongBox = false
        requireUserAuth = false
    }

    private val encryptionKit by lazy {
        EncryptionkitManager.Builder().build(encryptionConfig)
    }

    /**
     * Default [EncryptionProvider] that delegates encryption and decryption
     * to the EncryptionKit module.
     *
     * It uses a symmetric AES-GCM scheme provided by EncryptionKit and encodes
     * the result (IV + Ciphertext) into a Hex-delimited string.
     */
    private val defaultEncrypter: EncryptionProvider by lazy {
        object : EncryptionProvider {

            init {
                encryptionKit
            }

            override fun encrypt(data: String): String = runBlocking {
                val secureBytes = SecureBytes(data.toByteArray())
                val result = encryptionKit.encrypt(secureBytes).getOrThrow()

                val ivHex = result.iv.toHex()
                val ciphertextHex = result.ciphertext.toHex()

                "$ivHex$DELIMITER$ciphertextHex"
            }

            override fun decrypt(data: String): String = runBlocking {
                val parts = data.split(DELIMITER)
                require(parts.size == PARTS) { "Invalid encrypted data format. Expected IV:Ciphertext" }
                val iv = parts[0].fromHex()
                val ciphertext = parts[1].fromHex()

                val decryptedBytes = encryptionKit.decrypt(ciphertext, iv).getOrThrow()
                String(decryptedBytes)
            }
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray {
        check(length % PARTS == 0) { "Must have an even length" }
        return chunked(PARTS)
            .map { it.toInt(RADIX).toByte() }
            .toByteArray()
    }
}
