package es.joshluq.authkit.session.domain.interactor

import es.joshluq.authkit.session.event.SessionEvent
import es.joshluq.authkit.session.event.SessionEventBus
import es.joshluq.authkit.session.sdk.InteractionConfig
import es.joshluq.foundationkit.log.Loggerkit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

/**
 * Interactor responsible for notifying user activity to the SessionEventBus.
 * Implements throttling to avoid excessive events.
 */
class SessionInteractionInteractor internal constructor(
    private val scope: CoroutineScope,
    private val eventBus: SessionEventBus,
    private val config: InteractionConfig,
    private val logger: Loggerkit
) {
    private val lastNotificationTime = AtomicLong(0L)

    /**
     * Notifies user activity. The event will be throttled based on [InteractionConfig.throttleIntervalMillis].
     */
    fun notify() {
        if (!config.enabled) return

        val currentTime = System.currentTimeMillis()
        val lastTime = lastNotificationTime.get()

        if (currentTime - lastTime >= config.throttleIntervalMillis) {
            if (lastNotificationTime.compareAndSet(lastTime, currentTime)) {
                logger.i(TAG, "User activity detected, notifying bus.")
                scope.launch {
                    eventBus.emit(SessionEvent.UserActivity)
                }
            }
        }
    }

    companion object {
        private const val TAG = "SessionInteractionInteractor"
    }
}
