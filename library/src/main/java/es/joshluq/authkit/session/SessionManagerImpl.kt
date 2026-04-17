package es.joshluq.authkit.session

import es.joshluq.authkit.session.model.SessionInfo
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.SessionTimerConfiguration
import es.joshluq.authkit.session.storage.SessionStorage
import es.joshluq.authkit.session.worker.SessionWorkerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe implementation of [SessionManager] with proactive expiration warning.
 */
@Singleton
class SessionManagerImpl<T> @Inject constructor(
    private val storage: SessionStorage,
    private val workerManager: SessionWorkerManager
) : SessionManager<T> {

    private val mutex = Mutex()

    @Volatile
    private var configuration: SessionTimerConfiguration? = null

    private val _state = MutableStateFlow<SessionState<T>>(SessionState.Idle)
    override val state: StateFlow<SessionState<T>> = _state.asStateFlow()

    override fun addTimerConfiguration(configuration: SessionTimerConfiguration) {
        this.configuration = configuration
    }

    override suspend fun startSession(tokens: Map<String, String>, info: T) {
        mutex.withLock {
            storage.saveTokens(tokens)
            _state.value = SessionState.Active(SessionInfo(tokens, info))

            configuration?.let {
                workerManager.scheduleExpirationWithWarning(
                    it.totalDuration,
                    it.warningBefore
                )
            }
        }
    }

    override suspend fun extendSession(newTokens: Map<String, String>) {
        mutex.withLock {
            val info = when (val currentState = _state.value) {
                is SessionState.Active -> currentState.session.info
                is SessionState.ExpiringSoon -> currentState.session.info
                else -> null
            }

            if (info != null) {
                storage.saveTokens(newTokens)
                _state.value = SessionState.Active(SessionInfo(newTokens, info))

                configuration?.let {
                    workerManager.cancelExpiration()
                    workerManager.scheduleExpirationWithWarning(
                        it.totalDuration,
                        it.warningBefore
                    )
                }
            }
        }
    }

    override suspend fun endSession() {
        mutex.withLock {
            storage.clear()
            workerManager.cancelExpiration()
            _state.value = SessionState.Idle
        }
    }

    override suspend fun emitWarning() {
        mutex.withLock {
            val currentState = _state.value
            if (currentState is SessionState.Active) {
                _state.value = SessionState.ExpiringSoon(currentState.session)
            }
        }
    }

    override suspend fun markAsExpired() {
        mutex.withLock {
            storage.clear()
            _state.value = SessionState.Expired
        }
    }
}
