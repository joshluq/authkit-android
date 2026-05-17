package es.joshluq.authkit.session.domain.lifecycle

import es.joshluq.authkit.di.AuthKitLocator
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.foundationkit.log.Loggerkit
import java.util.concurrent.atomic.AtomicLong

/**
 * Component responsible for notifying user activity to keep the session alive.
 * Implements throttling to avoid excessive notifications.
 */
class SessionKeepAlive internal constructor(
    private val policy: InteractionPolicy,
    private val logger: Loggerkit,
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) {
    private val lastNotificationTime = AtomicLong(0L)

    /**
     * Notifies user activity. The notification will be throttled based on [InteractionPolicy.Timed.throttleIntervalMillis].
     */
    fun notifyActivity() {
        val timedPolicy = policy as? InteractionPolicy.Timed ?: return

        val currentTime = timeProvider()
        val lastTime = lastNotificationTime.get()

        if (currentTime - lastTime >= timedPolicy.throttleIntervalMillis) {
            if (lastNotificationTime.compareAndSet(lastTime, currentTime)) {
                logger.i(TAG, "User activity detected, notifying SessionKit.")
                AuthKitLocator.resolveSessionKit().onUserActivityDetected()
            }
        }
    }

    companion object {
        private const val TAG = "SessionKeepAlive"
    }
}
