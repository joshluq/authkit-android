package es.joshluq.authkit.session.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import es.joshluq.authkit.session.sdk.SessionKit

/**
 * Custom WorkerFactory for AuthKit session-related workers.
 * Used for manual dependency injection of [SessionKit].
 */
class SessionWorkerFactory(private val sessionKit: SessionKit) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SessionExpirationWorker::class.java.name ->
                SessionExpirationWorker(appContext, workerParameters, sessionKit)
            SessionPreExpirationWorker::class.java.name ->
                SessionPreExpirationWorker(appContext, workerParameters, sessionKit)
            else -> null
        }
    }
}
