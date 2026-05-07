package es.joshluq.authkit.session.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import es.joshluq.authkit.session.event.SessionEvent
import es.joshluq.authkit.session.event.SessionEventBus

/**
 * Worker responsible for notifying the SessionEventBus about upcoming expiration.
 */
internal class SessionPreExpirationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val eventBus: SessionEventBus
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        eventBus.emit(SessionEvent.PreExpiration)
        return Result.success()
    }
}
