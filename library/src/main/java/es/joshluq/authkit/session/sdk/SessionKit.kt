package es.joshluq.authkit.session.sdk

import androidx.work.WorkManager
import es.joshluq.authkit.di.ComponentFactory
import es.joshluq.authkit.di.SessionKitComponent
import es.joshluq.authkit.di.SessionKitDefaults
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.authkit.session.domain.interactor.SessionInteractionInteractor
import es.joshluq.authkit.session.event.SessionEvent
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.session.worker.SessionWorkerFactory
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.Manager
import es.joshluq.foundationkit.manager.ManagerBuilder
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.usecase.NoneInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SessionKit internal constructor(
    config: SessionKitConfig,
    private val componentFactory: ComponentFactory = SessionKitDefaults.factory
) : Manager<SessionKitConfig>() {

    companion object {
        private const val TAG = "SessionKit"
    }

    private lateinit var component: SessionKitComponent

    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
        observeSessionEvents()
        component.logger.i(TAG, "SessionKit initialized successfully.")
    }

    private fun observeSessionEvents() {
        sessionScope.launch {
            component.sessionEventBus.events.collect { event ->
                handleSessionEvent(event)
            }
        }
    }

    private suspend fun handleSessionEvent(event: SessionEvent) {
        mutex.withLock {
            val currentState = _state.value
            when (event) {
                SessionEvent.PreExpiration -> {
                    if (currentState is SessionState.Active) {
                        component.logger.i(TAG, "Processing PreExpiration event.")
                        _state.value = SessionState.ExpiringSoon
                    }
                }
                SessionEvent.Expiration -> {
                    if (currentState !is SessionState.Idle) {
                        component.logger.i(TAG, "Processing Expiration event.")
                        endSessionInternal()
                    }
                }
                SessionEvent.UserActivity -> {
                    if (currentState !is SessionState.Idle) {
                        component.logger.i(TAG, "Processing UserActivity event. Resetting timers.")
                        startTimerIfNeeded()
                        _state.value = SessionState.Active
                    }
                }
            }
        }
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

    private fun startTimerIfNeeded() {
        when (val timerConfig = config.expiration) {
            ExpirationPolicy.Never -> Unit
            is ExpirationPolicy.Timed -> {
                component.logger.i(TAG, "Start Timer and Worker with duration: ${timerConfig.durationMillis}ms")
                component.sessionTimer.start(
                    timerConfig.durationMillis,
                    timerConfig.warningThresholdMillis
                )
                component.sessionWorker.scheduleExpirationWithWarning(
                    timerConfig.durationMillis,
                    timerConfig.warningThresholdMillis
                )
            }
        }
    }

    suspend fun endSession() {
        mutex.withLock {
            endSessionInternal()
        }
    }

    private suspend fun endSessionInternal() {
        component.logger.i(TAG, "Ending session and clearing data.")
        component.clearSessionUseCase(NoneInput).onSuccess {
            endTimerIfNeeded()
            _state.value = SessionState.Idle
        }
    }

    private fun endTimerIfNeeded() {
        when (config.expiration) {
            ExpirationPolicy.Never -> Unit
            is ExpirationPolicy.Timed -> {
                component.logger.i(TAG, "Stop Timer and Worker")
                component.sessionTimer.stop()
                component.sessionWorker.cancelExpiration()
            }
        }
    }

    suspend fun extendSession(tokens: TokenHolder) {
        mutex.withLock {
            if (state.value == SessionState.Idle) {
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

    /**
     * Factory for creating workers. Used for manual dependency injection.
     */
    fun workerFactory(): SessionWorkerFactory = SessionWorkerFactory(component.sessionEventBus)

    /**
     * Returns the interaction interactor to notify user activity.
     */
    fun interactionInteractor(): SessionInteractionInteractor = component.interactionInteractor

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
