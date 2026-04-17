package es.joshluq.authkit.session.worker

/**
 * Interface defining background task operations for AuthKit.
 */
interface SessionWorkerManager {
    /**
     * Schedules a session expiration task.
     */
    fun scheduleExpiration(delayMillis: Long)

    /**
     * Cancels any scheduled expiration tasks.
     */
    fun cancelExpiration()

}
