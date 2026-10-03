package es.joshluq.authkit.biometric.sdk

import androidx.biometric.BiometricPrompt
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.encryptionkit.data.provider.biometric.BiometricCryptoHelper
import es.joshluq.encryptionkit.domain.model.BiometricCryptoResult
import es.joshluq.encryptionkit.domain.model.SecureBytes
import es.joshluq.encryptionkit.sdk.EncryptionKit
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BiometricKitTest {
    private val helper: BiometricCryptoHelper = mockk()
    private val cryptoObject: BiometricPrompt.CryptoObject = mockk()
    private val config =
        BiometricKitConfig.build {
            keyAlias = "test_biometric_key"
            invalidatedByEnrollment = false
        }

    private lateinit var biometricKit: BiometricKit

    @Before
    fun setUp() {
        biometricKit = BiometricKit(config, helper)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `config should have configured values`() {
        assertEquals("test_biometric_key", config.keyAlias)
        assertEquals(false, config.invalidatedByEnrollment)
    }

    @Test
    fun `default config should have sensible defaults`() {
        val defaultConfig = BiometricKitConfig.build {}
        assertEquals(BiometricKitConfig.DEFAULT_KEY_ALIAS, defaultConfig.keyAlias)
        assertTrue(defaultConfig.invalidatedByEnrollment)
    }

    @Test
    fun `install should obtain helper from AuthKit and return BiometricKit instance`() {
        val authKit: AuthKit = mockk()
        val encryptionKit: EncryptionKit = mockk()
        every { authKit.encryptionKit } returns encryptionKit
        every { encryptionKit.createBiometricCryptoHelper() } returns helper

        val installed = BiometricKit.install(authKit, config)
        assertNotNull(installed)
    }

    @Test
    fun `createEncryptCryptoObject should delegate to helper with config alias`() {
        every { helper.createEncryptCryptoObject("test_biometric_key") } returns cryptoObject

        val result = biometricKit.createEncryptCryptoObject()

        assertEquals(cryptoObject, result)
        verify { helper.createEncryptCryptoObject("test_biometric_key") }
    }

    @Test
    fun `createEncryptCryptoObject should delegate to helper with custom alias`() {
        every { helper.createEncryptCryptoObject("custom_alias") } returns cryptoObject

        val result = biometricKit.createEncryptCryptoObject("custom_alias")

        assertEquals(cryptoObject, result)
        verify { helper.createEncryptCryptoObject("custom_alias") }
    }

    @Test
    fun `createDecryptCryptoObject should delegate to helper with config alias and iv`() {
        val iv = byteArrayOf(1, 2, 3)
        every { helper.createDecryptCryptoObject("test_biometric_key", iv) } returns cryptoObject

        val result = biometricKit.createDecryptCryptoObject(iv = iv)

        assertEquals(cryptoObject, result)
        verify { helper.createDecryptCryptoObject("test_biometric_key", iv) }
    }

    @Test
    fun `encrypt should delegate to helper and return crypto result`() {
        val secret = "super_secret".toByteArray()
        val expectedResult = BiometricCryptoResult(ciphertext = byteArrayOf(10, 20), iv = byteArrayOf(1, 2))
        every { helper.encrypt(cryptoObject, any()) } returns expectedResult

        val result = biometricKit.encrypt(cryptoObject, secret)

        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        verify { helper.encrypt(cryptoObject, match { it.data.contentEquals(secret) }) }
    }

    @Test
    fun `decrypt should delegate to helper and return plaintext bytes`() {
        val ciphertext = byteArrayOf(10, 20)
        val expectedBytes = "super_secret".toByteArray()
        val plainBytes = expectedBytes.copyOf()
        val secureBytes = SecureBytes(plainBytes)
        every { helper.decrypt(cryptoObject, ciphertext) } returns secureBytes

        val result = biometricKit.decrypt(cryptoObject, ciphertext)

        assertTrue(result.isSuccess)
        assertArrayEquals(expectedBytes, result.getOrNull())
        assertTrue(secureBytes.isWiped())
        verify { helper.decrypt(cryptoObject, ciphertext) }
    }
}
