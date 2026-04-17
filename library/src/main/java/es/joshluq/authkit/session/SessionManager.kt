package es.joshluq.authkit.session

import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.SessionTimerConfiguration
import es.joshluq.authkit.session.model.SessionTimerDefaults
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
     * Adds a timer configuration for session expiration and warning.
     */
    fun addTimerConfiguration(configuration: SessionTimerConfiguration = SessionTimerDefaults.Default)

    /**
     * Starts a new session with the provided tokens and information.
     *
     * @param tokens Map of tokens to be stored.
     * @param info Additional user information.
     */
    suspend fun startSession(tokens: Map<String, String>, info: T)

    /**
     * Extends the current session with new tokens.
     */
    suspend fun extendSession(newTokens: Map<String, String>)

    /**
     * Ends the current session and clears persisted data.
     */
    suspend fun endSession()

    /**
     * Emits a warning that the session is about to expire.
     */
    suspend fun emitWarning()

    /**
     * Marks the session as expired and clears data.
     */
    suspend fun markAsExpired()
}
