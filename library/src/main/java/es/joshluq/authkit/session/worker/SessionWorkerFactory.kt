package es.joshluq.authkit.session.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import es.joshluq.authkit.session.event.SessionEventBus

/**
 * Custom WorkerFactory for AuthKit session-related workers.
 */
class SessionWorkerFactory internal constructor(private val eventBus: SessionEventBus) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SessionExpirationWorker::class.java.name ->
                SessionExpirationWorker(appContext, workerParameters, eventBus)
            SessionPreExpirationWorker::class.java.name ->
                SessionPreExpirationWorker(appContext, workerParameters, eventBus)
            else -> null
        }
    }
}
