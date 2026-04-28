package es.joshluq.authkit.session.sdk

import androidx.work.WorkManager
import es.joshluq.authkit.di.ComponentFactory
import es.joshluq.authkit.di.SessionKitComponent
import es.joshluq.authkit.di.SessionKitDefaults
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.session.worker.SessionWorkerFactory
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.Manager
import es.joshluq.foundationkit.manager.ManagerBuilder
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.usecase.NoneInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionKit internal constructor(
    config: SessionKitConfig,
    private val componentFactory: ComponentFactory = SessionKitDefaults.factory
): Manager<SessionKitConfig>(){

    companion object {
        private const val TAG = "SessionKit"
    }

    private lateinit var component: SessionKitComponent

    private val mutex = Mutex()

    private val _state = MutableStateFlow<SessionState>(SessionState.Idle)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        this.config = config
    }

    fun initialize(
        persistentStorage: StorageProvider,
        transientStorage: StorageProvider,
        workManager: WorkManager,
        logger: Loggerkit,
    ) {
        this.component = componentFactory(config, persistentStorage, transientStorage, workManager, logger)
        component.logger.i(TAG, "SessionKit initialized successfully.")
    }

    suspend fun startSession(tokens: TokenHolder) {
        mutex.withLock {
            component.logger.i(TAG, "Starting new session.")
            val input = SaveTokensUseCase.Input(tokens)
            component.saveTokensUseCase(input).onSuccess {
                startTimerIfNeeded()
                _state.value = SessionState.Active
            }
        }
    }

    private fun startTimerIfNeeded(){
        when(val timerConfig = config.expiration){
            ExpirationPolicy.Never -> Unit
            is ExpirationPolicy.Timed -> {
                component.logger.i(TAG, "Start Timer with duration: ${timerConfig.durationMillis}ms")
                component.sessionWorker.scheduleExpirationWithWarning(
                    timerConfig.durationMillis,
                    timerConfig.warningThresholdMillis
                )
            }
        }
    }

    suspend fun endSession() {
        mutex.withLock {
            component.logger.i(TAG, "Ending session and clearing data.")
            component.clearSessionUseCase(NoneInput).onSuccess {
                endTimerIfNeeded()
                _state.value = SessionState.Idle
            }
        }
    }

    private fun endTimerIfNeeded(){
        when(config.expiration){
            ExpirationPolicy.Never -> Unit
            is ExpirationPolicy.Timed -> {
                component.logger.i(TAG,"Stop Timer")
                component.sessionWorker.cancelExpiration()
            }
        }
    }

    suspend fun extendSession(tokens: TokenHolder) {
        mutex.withLock {
            if(state.value != SessionState.Active){
                component.logger.i(TAG, "Cannot extend session: current state is ${state.value}")
                return
            }
            component.logger.i(TAG, "Extending session with new tokens.")
            val input = SaveTokensUseCase.Input(tokens)
            component.saveTokensUseCase(input).onSuccess {
                startTimerIfNeeded()
                _state.value = SessionState.Active
            }
        }
    }

    suspend fun emitWarning() {
        mutex.withLock {
            val currentState = _state.value
            if (currentState is SessionState.Active) {
                _state.value = SessionState.ExpiringSoon
            }
        }
    }

    /**
     * Factory for creating workers. Used for manual dependency injection.
     */
    internal fun workerFactory(): SessionWorkerFactory = SessionWorkerFactory(this)


    internal class Builder : ManagerBuilder<SessionKitConfig> {

        /**
         * Builds and initializes a new instance of [SessionKit].
         *
         * @param config The [SessionKitConfig] required to configure the SDK.
         * @return A fully initialized [SessionKit] instance.
         */
        override fun build(config: SessionKitConfig): SessionKit {
            return SessionKit(config)
        }
    }
}