package es.joshluq.authkit.session.model

/**
 * Configuration for session expiration and warning timers.
 *
 * @param totalDuration Total session duration in milliseconds.
 * @param warningBefore Milliseconds before expiration to trigger a warning.
 */
data class SessionTimerConfiguration(
    val totalDuration: Long,
    val warningBefore: Long
)

object SessionTimerDefaults {
    /**
     * Default configuration for session expiration and warning timers.
     */
    val Default = SessionTimerConfiguration(
        totalDuration = 5 * 60 * 1000, // 5 minutes
        warningBefore = 60 * 1000 // 1 minute
    )
}
