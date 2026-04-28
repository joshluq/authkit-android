package es.joshluq.authkit.session.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SessionWorkerManagerImpl(
    private val workManager: WorkManager
) : SessionWorkerManager {

    override fun scheduleExpirationWithWarning(totalDuration: Long, warningBefore: Long?) {
        if (warningBefore != null && warningBefore < totalDuration) {
            val warningDelay = totalDuration - warningBefore

            val warningRequest = OneTimeWorkRequestBuilder<SessionPreExpirationWorker>()
                .setInitialDelay(warningDelay, TimeUnit.MILLISECONDS)
                .addTag(EXPIRATION_WORK_TAG)
                .build()

            val expirationRequest = OneTimeWorkRequestBuilder<SessionExpirationWorker>()
                .setInitialDelay(warningBefore, TimeUnit.MILLISECONDS)
                .addTag(EXPIRATION_WORK_TAG)
                .build()

            workManager.beginUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                warningRequest
            ).then(expirationRequest).enqueue()
        } else {
            val expirationRequest = OneTimeWorkRequestBuilder<SessionExpirationWorker>()
                .setInitialDelay(totalDuration, TimeUnit.MILLISECONDS)
                .addTag(EXPIRATION_WORK_TAG)
                .build()

            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                expirationRequest
            )
        }
    }

    override fun cancelExpiration() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    override fun cancelAllWork() {
        workManager.cancelAllWorkByTag(EXPIRATION_WORK_TAG)
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    companion object {
        private const val EXPIRATION_WORK_TAG = "session_expiration_work"
        private const val UNIQUE_WORK_NAME = "auth_session_lifecycle"
    }
}
