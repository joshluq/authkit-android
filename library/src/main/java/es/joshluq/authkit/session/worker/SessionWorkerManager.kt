package es.joshluq.authkit.session.worker

/**
 * Interface defining background task operations for AuthKit.
 */
interface SessionWorkerManager {
    /**
     * Schedules a session expiration task with a proactive warning.
     * @param totalDuration Total session duration in milliseconds.
     * @param warningBefore Milliseconds before the end to trigger a warning.
     */
    fun scheduleExpirationWithWarning(totalDuration: Long, warningBefore: Long)

    /**
     * Cancels any scheduled expiration tasks.
     */
    fun cancelExpiration()

    /**
     * Cancels all background work related to AuthKit.
     */
    fun cancelAllWork()
}
