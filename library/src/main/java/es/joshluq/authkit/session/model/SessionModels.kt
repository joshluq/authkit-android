package es.joshluq.authkit.session.model

/**
 * Represents the current state of a user session.
 *
 * @param T The type of the additional user information.
 */
sealed interface SessionState<out T> {

    /** Session is active and valid. */
    data class Active<T>(val session: Session<T>) : SessionState<T>

    /**
     * Session is about to expire.
     * @param session The current session data still available for use.
     */
    data class ExpiringSoon<T>(val session: Session<T>) : SessionState<T>

    /**
     * Initial state or state after a manual logout.
     * Technical equivalent to [Expired] as both represent a lack of active session.
     */
    data object Idle : SessionState<Nothing>

    /**
     * State reached when the session lifecycle naturally ends due to timeout.
     * Technical equivalent to [Idle] as both represent a lack of active session.
     */
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
