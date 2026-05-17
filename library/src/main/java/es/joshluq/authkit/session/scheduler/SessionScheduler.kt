package es.joshluq.authkit.session.scheduler

/**
 * Interface for scheduling session-related tasks (like expiration alarms).
 */
internal interface SessionScheduler {
    /**
     * Schedules a session expiration task with an optional proactive warning.
     * @param totalDuration Total session duration in milliseconds.
     * @param warningBefore Milliseconds before the end to trigger a warning.
     */
    fun schedule(totalDuration: Long, warningBefore: Long? = null)

    /**
     * Cancels all scheduled tasks.
     */
    fun cancelAll()
}
