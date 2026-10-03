package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.authkit.biometric.sdk.BiometricKit
import es.joshluq.authkit.biometric.sdk.BiometricKitConfig
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.encryptionkit.sdk.EncryptionKit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthKitTest {
    private val context: Context = mockk(relaxed = true)
    private val appContext: Context = mockk(relaxed = true)
    private val encryptionKit: EncryptionKit = mockk(relaxed = true)

    @Before
    fun setUp() {
        every { context.applicationContext } returns appContext
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init should build AuthKit with defaults`() {
        val authKit =
            AuthKit.init(context) {
                storeName = "test_store"
            }

        assertNotNull(authKit)
        assertNotNull(authKit.encryptionKit)
    }

    @Test
    fun `plugin accessor should return installed plugins`() {
        mockkObject(SessionKit.Companion)
        val mockSessionKit: SessionKit = mockk()
        every { SessionKit.install(any(), any()) } returns mockSessionKit

        val authKit =
            AuthKit.init(context) {
                addFeature(SessionKit, SessionKitConfig.build {})
            }

        assertEquals(mockSessionKit, authKit.session)
        assertEquals(mockSessionKit, authKit.plugin<SessionKit>())
    }

    @Test(expected = IllegalStateException::class)
    fun `session should throw IllegalStateException when not installed`() {
        val authKit = AuthKit.init(context) {}
        authKit.session
    }

    @Test(expected = IllegalStateException::class)
    fun `biometric should throw IllegalStateException when not installed`() {
        val authKit = AuthKit.init(context) {}
        authKit.biometric
    }

    @Test
    fun `biometric accessor should return installed BiometricKit`() {
        mockkObject(BiometricKit.Companion)
        val mockBiometricKit: BiometricKit = mockk()
        every { BiometricKit.install(any(), any()) } returns mockBiometricKit

        val authKit =
            AuthKit.init(context) {
                addFeature(BiometricKit, BiometricKitConfig.build {})
            }

        assertEquals(mockBiometricKit, authKit.biometric)
    }

    @Test
    fun `rotateSessionStore should delegate to encryptionKit rotateKey`() =
        runTest {
            coEvery { encryptionKit.rotateKey() } returns Result.success(Unit)

            val authKit =
                AuthKit.init(context) {
                    this.encryptionKit = this@AuthKitTest.encryptionKit
                }

            val result = authKit.rotateSessionStore()

            assertTrue(result.isSuccess)
            coVerify { encryptionKit.rotateKey() }
        }

    @Test
    fun `rotateSessionStore with custom alias should delegate to encryptionKit rotateKey`() =
        runTest {
            coEvery { encryptionKit.rotateKey("new_key_alias") } returns Result.success(Unit)

            val authKit =
                AuthKit.init(context) {
                    this.encryptionKit = this@AuthKitTest.encryptionKit
                }

            val result = authKit.rotateSessionStore("new_key_alias")

            assertTrue(result.isSuccess)
            coVerify { encryptionKit.rotateKey("new_key_alias") }
        }

    @Test
    fun `plugin should return null for unregistered plugin type`() {
        val authKit = AuthKit.init(context) {}
        assertNull(authKit.plugin<String>())
    }
}
