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

/**
 * [SessionKit] is the core manager for user sessions within the AuthKit SDK.
 *
 * It orchestrates session lifecycle events, manages authentication tokens, and maintains
 * the current [SessionState]. It supports different persistence and expiration policies
 * defined in [SessionKitConfig].
 *
 * @param config The configuration for the session manager.
 * @param componentFactory A factory to create the internal dependency component.
 */
class SessionKit internal constructor(
    config: SessionKitConfig,
    private val componentFactory: ComponentFactory = SessionKitDefaults.factory
) : Manager<SessionKitConfig>() {

    companion object : AuthKitPlugin<SessionKitConfig, SessionKit> {
        private const val TAG = "SessionKit"

        /**
         * Installs the [SessionKit] plugin into an [AuthKit] instance.
         *
         * @param authKit The AuthKit instance where the plugin is being installed.
         * @param config The configuration for the SessionKit.
         * @return An initialized [SessionKit] instance.
         */
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

    /**
     * Internal dependency component providing access to use cases and services.
     */
    internal lateinit var component: SessionKitComponent

    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val mutex = Mutex()

    private val _state = MutableStateFlow<SessionState>(SessionState.Idle)

    /**
     * A [StateFlow] representing the current state of the user session.
     */
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        this.config = config
    }

    /**
     * Initializes the internal components and attempts to restore a previous session.
     */
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

    /**
     * Checks for existing tokens in storage to restore an active session state if available.
     */
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

    /**
     * Triggered when the session is close to expiration.
     * Updates the state to [SessionState.ExpiringSoon].
     */
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

    /**
     * Triggered when the session has expired.
     * Ends the session and updates the state to [SessionState.Idle].
     */
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

    /**
     * Triggered when user activity is detected.
     * Resets the expiration timers if the session is not idle.
     */
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

    /**
     * Starts a new user session with the provided tokens.
     *
     * @param tokens The tokens to be associated with the new session.
     */
    suspend fun startSession(tokens: TokenHolder = TokenHolder.withoutToken()) {
        mutex.withLock {
            component.logger.i(TAG, "Starting new session.")
            val input = SaveTokensUseCase.Input(tokens = tokens)
            component.saveTokensUseCase(input).onSuccess {
                startTimerIfNeeded()
                _state.value = SessionState.Active
            }
        }
    }

    /**
     * Starts the foreground timer and schedules the background alarm if the policy is [ExpirationPolicy.Timed].
     */
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

    /**
     * Ends the current user session manually.
     * Clears tokens and stops any active timers.
     */
    suspend fun endSession() {
        mutex.withLock {
            endSessionInternal()
        }
    }

    /**
     * Internal implementation for ending the session.
     */
    private suspend fun endSessionInternal() {
        component.logger.i(TAG, "Ending session and clearing data.")
        component.clearSessionUseCase(NoneInput).onSuccess {
            endTimerIfNeeded()
            _state.value = SessionState.Idle
        }
    }

    /**
     * Stops the active timers and cancels scheduled alarms.
     */
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

    /**
     * Extends the current active session by updating it with new tokens.
     *
     * @param tokens The new tokens to refresh the session.
     */
    suspend fun extendSession(tokens: TokenHolder = TokenHolder.withoutToken()) {
        mutex.withLock {
            if (state.value == SessionState.Idle) {
                component.logger.i(TAG, "Cannot extend session: current state is ${state.value}")
                return
            }
            component.logger.i(TAG, "Extending session with new tokens.")
            val input = SaveTokensUseCase.Input(tokens = tokens)
            component.saveTokensUseCase(input).onSuccess {
                startTimerIfNeeded()
                _state.value = SessionState.Active
            }
        }
    }

    /**
     * Returns the component used to keep the session alive by notifying user activity.
     *
     * @return A [SessionKeepAlive] instance.
     */
    fun keepAlive(): SessionKeepAlive = component.keepAlive
}
