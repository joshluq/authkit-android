package es.joshluq.authkit.session.event

/**
 * Events related to session lifecycle and expiration.
 */
sealed interface SessionEvent {
    /**
     * Event triggered when the session is about to expire.
     */
    data object PreExpiration : SessionEvent

    /**
     * Event triggered when the session has expired.
     */
    data object Expiration : SessionEvent

    /**
     * Event triggered when user activity is detected.
     */
    data object UserActivity : SessionEvent
}
