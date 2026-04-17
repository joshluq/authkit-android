package es.joshluq.authkit.session.model

/**
 * Represents the current state of a user session.
 *
 * @param T The type of the additional user information.
 */
sealed interface SessionState<out T> {
    /** No active session. */
    data object Idle : SessionState<Nothing>

    /** Session is active and valid. */
    data class Active<T>(val session: Session<T>) : SessionState<T>

    /** Session has expired or been revoked. */
    data object Expired : SessionState<Nothing>
}

/**
 * Core session interface containing authentication tokens and extensible metadata.
 */
interface Session<out T> {
    /**
     * Map of tokens associated with this session (e.g., "accessToken", "refreshToken").
     */
    val tokens: Map<String, String>

    /**
     * Extensible user or session information.
     */
    val info: T
}

/**
 * Default implementation of [Session].
 */
data class SessionInfo<T>(
    override val tokens: Map<String, String> = emptyMap(),
    override val info: T
) : Session<T>
