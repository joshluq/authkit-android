package es.joshluq.authkit.session.sdk

import es.joshluq.authkit.di.SessionKitComponent
import es.joshluq.authkit.session.domain.lifecycle.SessionTimer
import es.joshluq.authkit.session.domain.usecase.ClearSessionUseCase
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.SessionState
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.session.scheduler.SessionScheduler
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.usecase.NoneOutput
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionKitTest {

    private lateinit var sessionKit: SessionKit
    private val config = SessionKitConfig.build {
        expiration = ExpirationPolicy.Timed(durationMillis = 1000)
    }

    private val component: SessionKitComponent = mockk(relaxed = true)
    private val saveTokensUseCase: SaveTokensUseCase = mockk()
    private val clearSessionUseCase: ClearSessionUseCase = mockk()
    private val sessionTimer: SessionTimer = mockk(relaxed = true)
    private val sessionScheduler: SessionScheduler = mockk(relaxed = true)
    private val logger: Loggerkit = mockk(relaxed = true)

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { component.saveTokensUseCase } returns saveTokensUseCase
        every { component.clearSessionUseCase } returns clearSessionUseCase
        every { component.sessionTimer } returns sessionTimer
        every { component.sessionScheduler } returns sessionScheduler
        every { component.logger } returns logger

        sessionKit = SessionKit(config) { _, _, _, _, _ -> component }
        sessionKit.component = component
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `startSession should move state to Active and start timers on success`() = runTest {
        val tokens = TokenHolder()
        coEvery { saveTokensUseCase(any()) } returns Result.success(NoneOutput)

        sessionKit.startSession(tokens)

        assertEquals(SessionState.Active, sessionKit.state.value)
        verify { sessionTimer.start(1000, null) }
        verify { sessionScheduler.schedule(1000, null) }
    }

    @Test
    fun `endSession should move state to Idle and stop timers on success`() = runTest {
        coEvery { saveTokensUseCase(any()) } returns Result.success(NoneOutput)
        sessionKit.startSession(TokenHolder())

        coEvery { clearSessionUseCase(any()) } returns Result.success(NoneOutput)

        sessionKit.endSession()

        assertEquals(SessionState.Idle, sessionKit.state.value)
        verify { sessionTimer.stop() }
        verify { sessionScheduler.cancelAll() }
    }

    @Test
    fun `onPreExpirationDetected should move state to ExpiringSoon if Active`() = runTest {
        coEvery { saveTokensUseCase(any()) } returns Result.success(NoneOutput)
        sessionKit.startSession(TokenHolder())

        sessionKit.onPreExpirationDetected()

        assertEquals(SessionState.ExpiringSoon, sessionKit.state.value)
    }

    @Test
    fun `onExpirationDetected should move state to Idle if not Idle`() = runTest {
        coEvery { saveTokensUseCase(any()) } returns Result.success(NoneOutput)
        sessionKit.startSession(TokenHolder())
        coEvery { clearSessionUseCase(any()) } returns Result.success(NoneOutput)

        sessionKit.onExpirationDetected()

        assertEquals(SessionState.Idle, sessionKit.state.value)
    }

    @Test
    fun `onUserActivityDetected should reset timers and move state to Active if not Idle`() = runTest {
        coEvery { saveTokensUseCase(any()) } returns Result.success(NoneOutput)
        sessionKit.startSession(TokenHolder())
        sessionKit.onPreExpirationDetected()
        assertEquals(SessionState.ExpiringSoon, sessionKit.state.value)

        sessionKit.onUserActivityDetected()

        assertEquals(SessionState.Active, sessionKit.state.value)
        verify(atLeast = 2) { sessionTimer.start(any(), any()) }
    }
}
