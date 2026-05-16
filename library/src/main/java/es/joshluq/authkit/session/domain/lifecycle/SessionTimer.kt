package es.joshluq.authkit.session.domain.lifecycle

import es.joshluq.authkit.session.event.SessionEvent
import es.joshluq.authkit.session.event.SessionEventBus
import es.joshluq.foundationkit.log.Loggerkit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interface for the session timer that handles foreground expiration.
 */
internal interface SessionTimer {
    fun start(durationMillis: Long, warningThresholdMillis: Long?)
    fun stop()
}

/**
 * Implementation of [SessionTimer] using Coroutines.
 */
internal class SessionTimerImpl(
    private val scope: CoroutineScope,
    private val eventBus: SessionEventBus,
    private val logger: Loggerkit
) : SessionTimer {

    private var timerJob: Job? = null

    override fun start(durationMillis: Long, warningThresholdMillis: Long?) {
        stop()
        timerJob = scope.launch {
            logger.i(TAG, "Starting session timer: duration=$durationMillis, warning=$warningThresholdMillis")
            if (warningThresholdMillis != null && warningThresholdMillis < durationMillis) {
                val warningDelay = durationMillis - warningThresholdMillis
                delay(warningDelay)
                logger.i(TAG, "Emitting PreExpiration event from timer")
                eventBus.emit(SessionEvent.PreExpiration)
                delay(warningThresholdMillis)
            } else {
                delay(durationMillis)
            }
            logger.i(TAG, "Emitting Expiration event from timer")
            eventBus.emit(SessionEvent.Expiration)
        }
    }

    override fun stop() {
        if (timerJob?.isActive == true) {
            logger.i(TAG, "Stopping session timer")
            timerJob?.cancel()
        }
        timerJob = null
    }

    companion object {
        private const val TAG = "SessionTimer"
    }
}
