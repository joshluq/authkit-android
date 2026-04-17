package es.joshluq.authkit.session

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import es.joshluq.authkit.session.model.SessionInfo
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.storage.SessionStorage
import es.joshluq.authkit.session.worker.SessionExpirationWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe implementation of [SessionManager].
 */
@Singleton
class SessionManagerImpl<T> @Inject constructor(
    private val storage: SessionStorage,
    private val workManager: WorkManager
) : SessionManager<T> {

    private val mutex = Mutex()
    private val _state = MutableStateFlow<SessionState<T>>(SessionState.Idle)
    override val state: StateFlow<SessionState<T>> = _state.asStateFlow()

    override suspend fun startSession(tokens: Map<String, String>, info: T, expirationMillis: Long?) {
        mutex.withLock {
            storage.saveTokens(tokens)
            _state.value = SessionState.Active(SessionInfo(tokens, info))
            
            expirationMillis?.let {
                scheduleExpiration(it)
            }
        }
    }

    override suspend fun updateTokens(tokens: Map<String, String>) {
        mutex.withLock {
            val currentState = _state.value
            if (currentState is SessionState.Active) {
                storage.saveTokens(tokens)
                _state.value = SessionState.Active(SessionInfo(tokens, currentState.session.info))
            }
        }
    }

    override suspend fun endSession() {
        mutex.withLock {
            storage.clear()
            cancelExpiration()
            _state.value = SessionState.Idle
        }
    }

    override suspend fun markAsExpired() {
        mutex.withLock {
            _state.value = SessionState.Expired
        }
    }

    private fun scheduleExpiration(delayMillis: Long) {
        val expirationRequest = OneTimeWorkRequestBuilder<SessionExpirationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(EXPIRATION_WORK_TAG)
            .build()

        workManager.enqueue(expirationRequest)
    }

    private fun cancelExpiration() {
        workManager.cancelAllWorkByTag(EXPIRATION_WORK_TAG)
    }

    companion object {
        private const val EXPIRATION_WORK_TAG = "session_expiration_work"
    }
}
