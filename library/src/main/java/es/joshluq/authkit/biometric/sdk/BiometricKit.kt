package es.joshluq.authkit.biometric.sdk

import androidx.biometric.BiometricPrompt
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.sdk.AuthKitPlugin
import es.joshluq.encryptionkit.data.provider.biometric.BiometricCryptoHelper
import es.joshluq.encryptionkit.domain.model.BiometricCryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.foundationkit.manager.Manager

/**
 * Plugin that provides hardware-backed biometric cryptographic capabilities using Android KeyStore
 * and [BiometricPrompt.CryptoObject].
 *
 * @param config The configuration for the biometric plugin.
 * @param biometricHelper The underlying helper from EncryptionKit.
 */
class BiometricKit internal constructor(
    config: BiometricKitConfig,
    private val biometricHelper: BiometricCryptoHelper,
) : Manager<BiometricKitConfig>() {
    companion object : AuthKitPlugin<BiometricKitConfig, BiometricKit> {
        /**
         * Installs the [BiometricKit] plugin into an [AuthKit] instance.
         *
         * @param authKit The AuthKit instance where the plugin is being installed.
         * @param config The configuration for BiometricKit.
         * @return The configured [BiometricKit] instance.
         */
        override fun install(
            authKit: AuthKit,
            config: BiometricKitConfig,
        ): BiometricKit {
            val helper = authKit.encryptionKit.createBiometricCryptoHelper()
            return BiometricKit(config, helper)
        }
    }

    init {
        this.config = config
    }

    /**
     * Creates a [BiometricPrompt.CryptoObject] initialized for encryption.
     * The hardware-bound key is generated or retrieved from Android KeyStore.
     *
     * @param alias The alias of the key. Defaults to [BiometricKitConfig.keyAlias].
     * @return An authenticated [BiometricPrompt.CryptoObject].
     */
    fun createEncryptCryptoObject(alias: String = config.keyAlias): BiometricPrompt.CryptoObject =
        biometricHelper.createEncryptCryptoObject(alias)

    /**
     * Creates a [BiometricPrompt.CryptoObject] initialized for decryption.
     *
     * @param alias The alias of the key. Defaults to [BiometricKitConfig.keyAlias].
     * @param iv The initialization vector originally produced during encryption.
     * @return An authenticated [BiometricPrompt.CryptoObject].
     */
    fun createDecryptCryptoObject(
        alias: String = config.keyAlias,
        iv: ByteArray,
    ): BiometricPrompt.CryptoObject = biometricHelper.createDecryptCryptoObject(alias, iv)

    /**
     * Encrypts the provided secret bytes using the authenticated [cryptoObject].
     * Memory is protected using [SecureBytes].
     *
     * @param cryptoObject The authenticated CryptoObject from [BiometricPrompt.AuthenticationCallback].
     * @param secret The confidential payload to encrypt.
     * @return A [Result] containing the [BiometricCryptoResult] with ciphertext and IV.
     */
    fun encrypt(
        cryptoObject: BiometricPrompt.CryptoObject,
        secret: ByteArray,
    ): Result<BiometricCryptoResult> =
        runCatching {
            val secureBytes = SecureBytes(secret)
            try {
                biometricHelper.encrypt(cryptoObject, secureBytes)
            } finally {
                secureBytes.close()
            }
        }

    /**
     * Decrypts the provided ciphertext using the authenticated [cryptoObject].
     *
     * @param cryptoObject The authenticated CryptoObject from [BiometricPrompt.AuthenticationCallback].
     * @param ciphertext The encrypted payload.
     * @return A [Result] containing the decrypted plaintext byte array.
     */
    fun decrypt(
        cryptoObject: BiometricPrompt.CryptoObject,
        ciphertext: ByteArray,
    ): Result<ByteArray> =
        runCatching {
            val secureBytes = biometricHelper.decrypt(cryptoObject, ciphertext)
            try {
                secureBytes.data.copyOf()
            } finally {
                secureBytes.close()
            }
        }
}
