package es.joshluq.authkit.session.worker

/**
 * Interface defining background task operations for AuthKit.
 */
interface SessionWorkerManager {
    /**
     * Schedules a session expiration task with an optional proactive warning.
     * @param totalDuration Total session duration in milliseconds.
     * @param warningBefore Milliseconds before the end to trigger a warning. If null, no warning will be scheduled.
     */
    fun scheduleExpirationWithWarning(totalDuration: Long, warningBefore: Long? = null)

    /**
     * Cancels any scheduled expiration tasks.
     */
    fun cancelExpiration()

    /**
     * Cancels all background work related to AuthKit.
     */
    fun cancelAllWork()
}
