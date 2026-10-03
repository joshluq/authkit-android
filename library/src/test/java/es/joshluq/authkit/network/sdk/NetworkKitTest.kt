package es.joshluq.authkit.network.sdk

import es.joshluq.authkit.session.model.Token
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.encryptionkit.sdk.EncryptionKit
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class NetworkKitTest {
    private val sessionProvider: NetworkSessionProvider = mockk(relaxed = true)
    private val encryptionKit: EncryptionKit = mockk(relaxed = true)
    private val tokenRefresher: TokenRefresher = mockk(relaxed = true)

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `default config should have enableDPoP false and null providers`() {
        val config = NetworkKitConfig.build {}
        assertFalse(config.enableDPoP)
        assertNull(config.sessionProvider)
        assertNull(config.tokenRefresher)
    }

    @Test
    fun `interceptor should inject Authorization header when access token is present`() {
        val config =
            NetworkKitConfig.build {
                this.sessionProvider = this@NetworkKitTest.sessionProvider
            }
        val networkKit = NetworkKit(config, encryptionKit)

        val tokens = TokenHolder.withToken(Token.Access("jwt_token_123"))
        coEvery { sessionProvider.getTokens() } returns tokens

        val originalRequest = Request.Builder().url("https://api.example.com/data").build()
        val chain: Interceptor.Chain = mockk()
        val requestSlot = slot<Request>()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns mockk()

        networkKit.interceptor().intercept(chain)

        assertEquals("Bearer jwt_token_123", requestSlot.captured.header("Authorization"))
        assertNull(requestSlot.captured.header("DPoP"))
    }

    @Test
    fun `interceptor should not inject Authorization header when access token is missing`() {
        val config =
            NetworkKitConfig.build {
                this.sessionProvider = this@NetworkKitTest.sessionProvider
            }
        val networkKit = NetworkKit(config, encryptionKit)
        coEvery { sessionProvider.getTokens() } returns null

        val originalRequest = Request.Builder().url("https://api.example.com/data").build()
        val chain: Interceptor.Chain = mockk()
        val requestSlot = slot<Request>()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns mockk()

        networkKit.interceptor().intercept(chain)

        assertNull(requestSlot.captured.header("Authorization"))
        assertNull(requestSlot.captured.header("DPoP"))
    }

    @Test
    fun `interceptor with enableDPoP true should sign and attach DPoP header`() =
        runTest {
            val config =
                NetworkKitConfig.build {
                    this.sessionProvider = this@NetworkKitTest.sessionProvider
                    enableDPoP = true
                }
            val networkKit = NetworkKit(config, encryptionKit)
            val tokens = TokenHolder.withToken(Token.Access("dpop_access_token"))
            coEvery { sessionProvider.getTokens() } returns tokens

            val dummySignature = byteArrayOf(1, 2, 3, 4, 5)
            coEvery { encryptionKit.sign(any(), any()) } returns Result.success(dummySignature)

            val originalRequest = Request.Builder().url("https://api.example.com/secure").build()
            val chain: Interceptor.Chain = mockk()
            val requestSlot = slot<Request>()
            every { chain.request() } returns originalRequest
            every { chain.proceed(capture(requestSlot)) } returns mockk()

            networkKit.interceptor().intercept(chain)

            assertEquals("Bearer dpop_access_token", requestSlot.captured.header("Authorization"))
            assertNotNull(requestSlot.captured.header("DPoP"))
            coVerify { encryptionKit.sign(any(), any()) }
        }

    @Test
    fun `authenticator should refresh token and retry request on 401`() =
        runTest {
            val config =
                NetworkKitConfig.build {
                    this.sessionProvider = this@NetworkKitTest.sessionProvider
                    this.tokenRefresher = this@NetworkKitTest.tokenRefresher
                }
            val networkKit = NetworkKit(config, encryptionKit)

            val oldTokens = TokenHolder.withToken(Token.Access("old_access_token"))
            val newTokens = TokenHolder.withToken(Token.Access("new_access_token"))

            coEvery { sessionProvider.getTokens() } returns oldTokens
            coEvery { tokenRefresher.refresh(oldTokens) } returns Result.success(newTokens)

            val request =
                Request
                    .Builder()
                    .url("https://api.example.com/data")
                    .header("Authorization", "Bearer old_access_token")
                    .build()
            val response =
                Response
                    .Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .build()

            val retriedRequest = networkKit.authenticator().authenticate(null, response)

            assertNotNull(retriedRequest)
            assertEquals("Bearer new_access_token", retriedRequest?.header("Authorization"))
            coVerify { sessionProvider.saveTokens(newTokens) }
        }

    @Test
    fun `authenticator should clear session when refresh fails`() =
        runTest {
            val config =
                NetworkKitConfig.build {
                    this.sessionProvider = this@NetworkKitTest.sessionProvider
                    this.tokenRefresher = this@NetworkKitTest.tokenRefresher
                }
            val networkKit = NetworkKit(config, encryptionKit)

            val oldTokens = TokenHolder.withToken(Token.Access("expired_token"))
            coEvery { sessionProvider.getTokens() } returns oldTokens
            coEvery { tokenRefresher.refresh(oldTokens) } returns Result.failure(Exception("Refresh expired"))

            val request =
                Request
                    .Builder()
                    .url("https://api.example.com/data")
                    .header("Authorization", "Bearer expired_token")
                    .build()
            val response =
                Response
                    .Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(401)
                    .message("Unauthorized")
                    .build()

            val retriedRequest = networkKit.authenticator().authenticate(null, response)

            assertNull(retriedRequest)
            coVerify { sessionProvider.clearSession() }
        }
}
