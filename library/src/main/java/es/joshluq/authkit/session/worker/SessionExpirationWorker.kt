package es.joshluq.authkit.session.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import es.joshluq.authkit.session.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker responsible for marking the session as expired after a scheduled delay.
 */
@HiltWorker
class SessionExpirationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionManager: SessionManager<*>
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        sessionManager.markAsExpired()
        return Result.success()
    }
}
