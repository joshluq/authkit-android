package es.joshluq.authkit.session.sdk

import android.content.Context
import es.joshluq.authkit.di.ComponentFactory
import es.joshluq.authkit.di.SessionKitComponent
import es.joshluq.authkit.di.SessionKitDefaults
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.sdk.AuthKitPlugin
import es.joshluq.authkit.session.domain.lifecycle.SessionKeepAlive
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.Manager
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

    companion object : AuthKitPlugin<SessionKitConfig, SessionKit> {
        private const val TAG = "SessionKit"

        override fun install(authKit: AuthKit, config: SessionKitConfig): SessionKit {
            return SessionKit(config).apply {
                initialize(
                    authKit.component.persistentStorage,
                    authKit.component.transientStorage,
                    authKit.component.context,
                    authKit.component.logger
                )
            }
        }
    }

    internal lateinit var component: SessionKitComponent

    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val mutex = Mutex()

    private val _state = MutableStateFlow<SessionState>(SessionState.Idle)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        this.config = config
    }

    private fun initialize(
        persistentStorage: StorageProvider,
        transientStorage: StorageProvider,
        context: Context,
        logger: Loggerkit,
    ) {
        this.component = componentFactory(config, persistentStorage, transientStorage, context, logger)
        restoreSessionIfPossible()
        component.logger.i(TAG, "SessionKit initialized successfully.")
    }

    private fun restoreSessionIfPossible() {
        sessionScope.launch {
            mutex.withLock {
                component.getTokensUseCase(NoneInput).onSuccess { output ->
                    if (!output.tokens.isEmpty()) {
                        component.logger.i(TAG, "Active session detected during initialization. Restoring state.")
                        _state.value = SessionState.Active

                        if (config.expiration is ExpirationPolicy.Timed) {
                            startTimerIfNeeded()
                        }
                    } else {
                        _state.value = SessionState.Idle
                    }
                }.onFailure {
                    component.logger.e(TAG, "Failed to restore session: ${it.message}")
                    _state.value = SessionState.Idle
                }
            }
        }
    }

    internal fun onPreExpirationDetected() {
        sessionScope.launch {
            mutex.withLock {
                if (_state.value is SessionState.Active) {
                    component.logger.i(TAG, "Processing PreExpiration notification.")
                    _state.value = SessionState.ExpiringSoon
                }
            }
        }
    }

    internal fun onExpirationDetected() {
        sessionScope.launch {
            mutex.withLock {
                if (_state.value !is SessionState.Idle) {
                    component.logger.i(TAG, "Processing Expiration notification.")
                    endSessionInternal()
                }
            }
        }
    }

    internal fun onUserActivityDetected() {
        sessionScope.launch {
            mutex.withLock {
                if (_state.value !is SessionState.Idle) {
                    component.logger.i(TAG, "Processing UserActivity notification. Resetting timers.")
                    startTimerIfNeeded()
                    _state.value = SessionState.Active
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
                component.logger.i(TAG, "Start Timer and Scheduler with duration: ${timerConfig.durationMillis}ms")
                component.sessionTimer.start(
                    timerConfig.durationMillis,
                    timerConfig.warningThresholdMillis
                )
                component.sessionScheduler.schedule(
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
                component.logger.i(TAG, "Stop Timer and Scheduler")
                component.sessionTimer.stop()
                component.sessionScheduler.cancelAll()
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
     * Returns the component to keep the session alive by notifying user activity.
     */
    fun keepAlive(): SessionKeepAlive = component.keepAlive
}
