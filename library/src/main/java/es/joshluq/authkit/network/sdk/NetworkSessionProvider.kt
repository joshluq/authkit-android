package es.joshluq.authkit.network.sdk

import es.joshluq.authkit.session.model.TokenHolder

/**
 * Abstraction that provides session-related operations for the NetworkKit plugin.
 * This allows the NetworkKit to be decoupled from the concrete SessionKit implementation.
 */
interface NetworkSessionProvider {
    /**
     * Returns the current tokens.
     */
    suspend fun getTokens(): TokenHolder?

    /**
     * Saves new tokens (e.g., after a successful refresh).
     */
    suspend fun saveTokens(tokens: TokenHolder)

    /**
     * Clears the session (e.g., after a failed refresh).
     */
    suspend fun clearSession()
}
