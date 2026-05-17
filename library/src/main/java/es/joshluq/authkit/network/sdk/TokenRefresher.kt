package es.joshluq.authkit.network.sdk

import es.joshluq.authkit.session.model.TokenHolder

/**
 * Interface to be implemented by the consumer app to handle token refreshing.
 * The SDK will call this method when a 401 error is detected.
 */
interface TokenRefresher {
    /**
     * Executes the network call to refresh tokens.
     * @param oldTokens The current tokens in the session.
     * @return A [Result] containing the new [TokenHolder] on success.
     */
    suspend fun refresh(oldTokens: TokenHolder): Result<TokenHolder>
}
