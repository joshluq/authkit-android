package es.joshluq.authkit.showcase

import androidx.lifecycle.viewModelScope
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.Token
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.viewmodel.ScreenViewModel
import es.joshluq.foundationkit.viewmodel.UiEffect
import es.joshluq.foundationkit.viewmodel.UiEvent
import es.joshluq.foundationkit.viewmodel.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class SessionUiState(
    val sessionState: SessionState = SessionState.Initializing,
    val userProfile: UserProfile? = null,
    val rotationStatus: String? = null,
    val biometricStatus: String? = null,
    val secondsRemaining: Int = 0,
) : UiState

sealed interface SessionUiEvent : UiEvent {
    data class SaveMockProfile(val profile: UserProfile) : SessionUiEvent
    data object StartSession : SessionUiEvent
    data object ExtendSession : SessionUiEvent
    data object EndSession : SessionUiEvent
    data object SimulateActivity : SessionUiEvent
    data object RotateKeyStore : SessionUiEvent
    data object TestBiometrics : SessionUiEvent
}

sealed interface SessionUiEffect : UiEffect {
    data class ShowMessage(val message: String) : SessionUiEffect
}

class SessionViewModel(
    private val authKit: AuthKit,
    private val preset: SessionPreset,
) : ScreenViewModel<SessionUiState, SessionUiEvent, SessionUiEffect>() {

    private val isTimed = preset.expiration is ExpirationPolicy.Timed
    private val initialDurationSeconds: Int =
        if (preset.expiration is ExpirationPolicy.Timed) {
            (preset.expiration.durationMillis / MILLIS_PER_SECOND).toInt()
        } else {
            0
        }

    private var timerJob: Job? = null

    init {
        observeSessionState()
    }

    override fun createInitialState(): SessionUiState = SessionUiState()

    private fun observeSessionState() {
        viewModelScope.launch {
            authKit.session.state.collect { sessionState ->
                updateState { copy(sessionState = sessionState) }
                handleStateChange(sessionState)
            }
        }
    }

    private fun handleStateChange(sessionState: SessionState) {
        viewModelScope.launch {
            if (sessionState is SessionState.Active) {
                val profile = authKit.session.getSessionData<UserProfile>()
                updateState { copy(userProfile = profile) }
                startCountdown()
            } else {
                updateState { copy(userProfile = null, secondsRemaining = 0) }
                stopCountdown()
            }
        }
    }

    override fun handleEvent(event: SessionUiEvent) {
        when (event) {
            is SessionUiEvent.SaveMockProfile -> saveProfile(event.profile)
            SessionUiEvent.StartSession -> startSession()
            SessionUiEvent.ExtendSession -> extendSession()
            SessionUiEvent.EndSession -> endSession()
            SessionUiEvent.SimulateActivity -> simulateActivity()
            SessionUiEvent.RotateKeyStore -> rotateKeyStore()
            SessionUiEvent.TestBiometrics -> testBiometrics()
        }
    }

    private fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            authKit.session.saveSessionData(profile)
            updateState { copy(userProfile = profile) }
        }
    }

    private fun startSession() {
        viewModelScope.launch {
            val tokens = TokenHolder().apply {
                addToken(Token.Access("mock_access_token"))
                addToken(Token.Refresh("mock_refresh_token"))
            }
            authKit.session.startSession(tokens)
        }
    }

    private fun extendSession() {
        viewModelScope.launch {
            val tokens = TokenHolder().apply {
                addToken(Token.Access("new_access_token_${System.currentTimeMillis()}"))
            }
            authKit.session.extendSession(tokens)
            resetCountdown()
        }
    }

    private fun endSession() {
        viewModelScope.launch {
            authKit.session.endSession()
        }
    }

    private fun simulateActivity() {
        authKit.session.keepAlive().notifyActivity()
        resetCountdown()
    }

    private fun rotateKeyStore() {
        viewModelScope.launch {
            val result = authKit.rotateSessionStore()
            val status = if (result.isSuccess) {
                "Session KeyStore rotated! Historic keys active."
            } else {
                "Rotation error: ${result.exceptionOrNull()?.message}"
            }
            updateState { copy(rotationStatus = status) }
        }
    }

    private fun testBiometrics() {
        try {
            val cryptoObject = authKit.biometric.createEncryptCryptoObject()
            val status = "Biometric CryptoObject ready: ${cryptoObject.cipher?.algorithm}"
            updateState { copy(biometricStatus = status) }
        } catch (e: Exception) {
            val status = "Biometric info: ${e.message}"
            updateState { copy(biometricStatus = status) }
        }
    }

    private fun startCountdown() {
        if (!isTimed) return
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            updateState { copy(secondsRemaining = initialDurationSeconds) }
            while (true) {
                delay(TICK_INTERVAL_MS.milliseconds)
                val current = state.value.secondsRemaining
                if (current <= 1) {
                    updateState { copy(secondsRemaining = 0) }
                    break
                }
                updateState { copy(secondsRemaining = current - 1) }
            }
        }
    }

    private fun resetCountdown() {
        if (!isTimed) return
        startCountdown()
    }

    private fun stopCountdown() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        private const val MILLIS_PER_SECOND = 1000L
        private const val TICK_INTERVAL_MS = 1000L
    }
}
