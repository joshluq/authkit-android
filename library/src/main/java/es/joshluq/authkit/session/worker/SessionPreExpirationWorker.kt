package es.joshluq.authkit.session.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import es.joshluq.authkit.session.sdk.SessionKit

/**
 * Worker responsible for notifying the SessionManager about upcoming expiration.
 */
class SessionPreExpirationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val sessionKit: SessionKit
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        sessionKit.emitWarning()
        return Result.success()
    }
}
