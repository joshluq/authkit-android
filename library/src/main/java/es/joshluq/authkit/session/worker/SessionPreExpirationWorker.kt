package es.joshluq.authkit.session.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import es.joshluq.authkit.session.SessionManager

/**
 * Worker responsible for notifying the SessionManager about upcoming expiration.
 */
@HiltWorker
class SessionPreExpirationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionManager: SessionManager<Any>
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Notify the SessionManager about the warning
        sessionManager.emitWarning()

        return Result.success()
    }
}
