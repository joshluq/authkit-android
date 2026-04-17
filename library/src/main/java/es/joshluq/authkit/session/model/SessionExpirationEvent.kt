package es.joshluq.authkit.session.model

/**
 * Events emitted during the session expiration lifecycle.
 */
sealed class SessionExpirationEvent {
    /**
     * Emitted when the session is about to expire.
     * @param remainingMillis Milliseconds remaining until final expiration.
     */
    data class Warning(val remainingMillis: Long) : SessionExpirationEvent()

    /**
     * Emitted when the session has officially expired.
     */
    data object Expired : SessionExpirationEvent()
}
