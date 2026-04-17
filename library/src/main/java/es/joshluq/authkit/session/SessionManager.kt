package es.joshluq.authkit.session

import es.joshluq.authkit.session.model.SessionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface responsible for managing the user session lifecycle, identity, and persistence.
 *
 * @param T The type of the additional user information.
 */
interface SessionManager<T> {
    /**
     * Reactive stream of the current session state.
     */
    val state: StateFlow<SessionState<T>>

    /**
     * Starts a new session with the provided tokens and information.
     *
     * @param tokens Map of tokens to be stored.
     * @param info Additional user information.
     * @param expirationMillis Optional expiration time in milliseconds.
     */
    suspend fun startSession(tokens: Map<String, String>, info: T, expirationMillis: Long? = null)

    /**
     * Updates the current session tokens.
     */
    suspend fun updateTokens(tokens: Map<String, String>)

    /**
     * Ends the current session and clears persisted data.
     */
    suspend fun endSession()

    /**
     * Marks the session as expired.
     */
    suspend fun markAsExpired()
}
