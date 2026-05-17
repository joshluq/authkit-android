package es.joshluq.authkit.network.sdk

import es.joshluq.authkit.di.AuthKitLocator
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.sdk.AuthKitPlugin
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.foundationkit.manager.Manager
import es.joshluq.foundationkit.usecase.NoneInput
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
    config: NetworkKitConfig
) : Manager<NetworkKitConfig>() {

    companion object : AuthKitPlugin<NetworkKitConfig, NetworkKit> {
        /**
         * Installs the [NetworkKit] plugin into an [AuthKit] instance.
         */
        override fun install(authKit: AuthKit, config: NetworkKitConfig): NetworkKit {
            return NetworkKit(config)
        }
    }

    init {
        this.config = config
    }

    /**
     * Returns an OkHttp [Interceptor] that automatically adds the Authorization header
     * with the current Access Token to every outgoing request.
     */
    fun interceptor(): Interceptor = Interceptor { chain ->
        val sessionKit = AuthKitLocator.resolveSessionKit()
        val tokens = runBlocking { sessionKit.component.getTokensUseCase(NoneInput).getOrNull()?.tokens }
        val accessToken = tokens?.getAccessToken()?.value

        val requestBuilder = chain.request().newBuilder()
        if (accessToken != null) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        chain.proceed(requestBuilder.build())
    }

    /**
     * Returns an OkHttp [Authenticator] that handles HTTP 401 Unauthorized errors.
     * It attempts a silent refresh using the [TokenRefresher] provided in the config.
     * If successful, the original request is retried with the new token.
     */
    fun authenticator(): Authenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (response.countPriorResponses() >= 3) return null

            val sessionKit = AuthKitLocator.resolveSessionKit()
            val refresher = config.tokenRefresher ?: return null

            synchronized(this) {
                return runBlocking {
                    val currentTokens = sessionKit.component.getTokensUseCase(NoneInput).getOrNull()?.tokens
                        ?: return@runBlocking null

                    val accessToken = currentTokens.getAccessToken()?.value

                    val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                    if (accessToken != requestToken) {
                        return@runBlocking response.request.newBuilder()
                            .header("Authorization", "Bearer $accessToken")
                            .build()
                    }

                    val result = refresher.refresh(currentTokens)
                    val newTokens = result.getOrNull()

                    if (newTokens != null) {
                        val input = SaveTokensUseCase.Input(newTokens)
                        sessionKit.component.saveTokensUseCase(input)

                        val newAccessToken = newTokens.getAccessToken()?.value
                        if (newAccessToken != null) {
                            return@runBlocking response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        }
                    } else {
                        sessionKit.endSession()
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
}
