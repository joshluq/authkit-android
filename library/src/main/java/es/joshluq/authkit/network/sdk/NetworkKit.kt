package es.joshluq.authkit.network.sdk

import android.annotation.SuppressLint
import es.joshluq.authkit.di.AuthKitLocator
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.sdk.AuthKitPlugin
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.encryptionkit.sdk.EncryptionKit
import es.joshluq.foundationkit.manager.Manager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Plugin that provides network automation capabilities, such as automatic token injection
 * and silent token refresh using OkHttp.
 */
class NetworkKit internal constructor(
    config: NetworkKitConfig,
    private val encryptionKit: EncryptionKit? = null,
) : Manager<NetworkKitConfig>() {
    companion object : AuthKitPlugin<NetworkKitConfig, NetworkKit> {
        private const val MAX_RETRIES = 3
        private const val MILLIS_PER_SECOND = 1000L

        /**
         * Installs the [NetworkKit] plugin into an [AuthKit] instance.
         *
         * @param authKit The AuthKit instance where the plugin is installed.
         * @param config The configuration for the NetworkKit.
         * @return The configured and initialized [NetworkKit] instance.
         */
        override fun install(
            authKit: AuthKit,
            config: NetworkKitConfig,
        ): NetworkKit = NetworkKit(config, authKit.encryptionKit)
    }

    private val sessionProvider: NetworkSessionProvider by lazy {
        config.sessionProvider ?: DefaultSessionProvider(AuthKitLocator.resolveSessionKit())
    }

    private val refreshLock = Any()

    init {
        this.config = config
    }

    /**
     * Returns an OkHttp [Interceptor] that automatically adds the Authorization header
     * with the current Access Token to every outgoing request.
     * If DPoP is enabled, also signs and attaches the DPoP proof header.
     *
     * @return The [Interceptor] configured for token injection.
     */
    @SuppressLint("NewApi")
    fun interceptor(): Interceptor =
        Interceptor { chain ->
            val tokens = runBlocking { sessionProvider.getTokens() }
            val accessToken = tokens?.getAccessToken()?.value

            val requestBuilder = chain.request().newBuilder()
            if (accessToken != null) {
                requestBuilder.header("Authorization", "Bearer $accessToken")
            }

            if (config.enableDPoP && encryptionKit != null) {
                val method = chain.request().method
                val url = chain.request().url.toString()
                val timestamp = System.currentTimeMillis() / MILLIS_PER_SECOND
                val payload = "$method $url $timestamp".toByteArray()
                val signature = runBlocking { encryptionKit.sign(payload).getOrNull() }
                if (signature != null) {
                    val encoded =
                        runCatching {
                            java.util.Base64
                                .getUrlEncoder()
                                .withoutPadding()
                                .encodeToString(signature)
                        }.getOrElse {
                            android.util.Base64.encodeToString(
                                signature,
                                android.util.Base64.URL_SAFE or
                                    android.util.Base64.NO_WRAP or
                                    android.util.Base64.NO_PADDING,
                            )
                        }
                    requestBuilder.header("DPoP", encoded)
                }
            }

            chain.proceed(requestBuilder.build())
        }

    /**
     * Returns an OkHttp [Authenticator] that handles HTTP 401 Unauthorized errors.
     * It attempts a silent refresh using the [TokenRefresher] provided in the config.
     * If successful, the original request is retried with the new token.
     * If the refresh fails or the token refresher is not provided, the session is cleared.
     *
     * @return The [Authenticator] configured for silent token refresh.
     */
    fun authenticator(): Authenticator =
        object : Authenticator {
            override fun authenticate(
                route: Route?,
                response: Response,
            ): Request? {
                if (response.countPriorResponses() >= MAX_RETRIES) return null

                val refresher = config.tokenRefresher ?: return null

                synchronized(refreshLock) {
                    return runBlocking {
                        val currentTokens = sessionProvider.getTokens() ?: return@runBlocking null
                        val accessToken = currentTokens.getAccessToken()?.value

                        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                        if (accessToken != requestToken) {
                            return@runBlocking response.request
                                .newBuilder()
                                .header("Authorization", "Bearer $accessToken")
                                .build()
                        }

                        val result = refresher.refresh(currentTokens)
                        val newTokens = result.getOrNull()

                        if (newTokens != null) {
                            sessionProvider.saveTokens(newTokens)
                            val newAccessToken = newTokens.getAccessToken()?.value
                            if (newAccessToken != null) {
                                return@runBlocking response.request
                                    .newBuilder()
                                    .header("Authorization", "Bearer $newAccessToken")
                                    .build()
                            }
                        } else {
                            sessionProvider.clearSession()
                        }
                        null
                    }
                }
            }
        }

    private fun Response.countPriorResponses(): Int {
        var count = 0
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    /**
     * Internal implementation of the [NetworkSessionProvider] that delegates to [SessionKit].
     */
    private class DefaultSessionProvider(
        private val sessionKit: SessionKit,
    ) : NetworkSessionProvider {
        override suspend fun getTokens(): TokenHolder? = sessionKit.getTokens()

        override suspend fun saveTokens(tokens: TokenHolder) {
            sessionKit.extendSession(tokens)
        }

        override suspend fun clearSession() {
            sessionKit.endSession()
        }
    }
}
