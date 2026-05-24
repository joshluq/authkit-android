package es.joshluq.authkit.session.model

/**
 * Defines the expiration rules for a session.
 */
sealed interface ExpirationPolicy {
    /**
     * Indicates that the session never expires automatically.
     */
    data object Never : ExpirationPolicy

    /**
     * Indicates that the session expires after a specific duration of inactivity.
     *
     * @property durationMillis The total duration in milliseconds before the session expires.
     * @property warningThresholdMillis An optional threshold in milliseconds before
     * expiration to trigger a warning (e.g., [SessionState.ExpiringSoon]).
     */
    data class Timed(val durationMillis: Long, val warningThresholdMillis: Long? = null) : ExpirationPolicy
}
