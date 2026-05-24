package es.joshluq.authkit.network.sdk

import es.joshluq.authkit.session.model.TokenHolder

/**
 * Abstraction that provides session-related operations for the NetworkKit plugin.
 * This allows the NetworkKit to be decoupled from the concrete SessionKit implementation.
 */
interface NetworkSessionProvider {
    /**
     * Retrieves the current authentication tokens from the session.
     *
     * @return The [TokenHolder] containing the current tokens, or null if no session exists.
     */
    suspend fun getTokens(): TokenHolder?

    /**
     * Saves new authentication tokens, typically invoked after a successful token refresh.
     *
     * @param tokens The new [TokenHolder] to be saved in the session.
     */
    suspend fun saveTokens(tokens: TokenHolder)

    /**
     * Clears the current session, typically invoked when a token refresh fails and the user must be logged out.
     */
    suspend fun clearSession()
}
