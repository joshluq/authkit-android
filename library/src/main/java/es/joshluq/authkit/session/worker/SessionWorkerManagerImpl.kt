package es.joshluq.authkit.session.worker

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionWorkerManagerImpl @Inject constructor(
    private val workManager: WorkManager
) : SessionWorkerManager {

    override fun scheduleExpiration(delayMillis: Long) {
        val expirationRequest = OneTimeWorkRequestBuilder<SessionExpirationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(EXPIRATION_WORK_TAG)
            .build()

        workManager.enqueue(expirationRequest)
    }

    override fun cancelExpiration() {
        workManager.cancelAllWorkByTag(EXPIRATION_WORK_TAG)
    }

    companion object {
        private const val EXPIRATION_WORK_TAG = "session_expiration_work"
    }
}
