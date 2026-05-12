package es.joshluq.authkit.session.domain.interactor

import es.joshluq.authkit.session.event.SessionEvent
import es.joshluq.authkit.session.event.SessionEventBus
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.foundationkit.log.Loggerkit
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionInteractionInteractorTest {

    private val eventBus: SessionEventBus = mockk(relaxed = true)
    private val logger: Loggerkit = mockk(relaxed = true)
    private val testScope = TestScope()

    @Test
    fun `notifyActivity should emit UserActivity event when enabled`() = testScope.runTest {
        val policy = InteractionPolicy.Timed(throttleIntervalMillis = 0)
        val interactor = SessionInteractionInteractor(
            testScope, eventBus, policy, logger,
            timeProvider = { 1000L }
        )

        interactor.notifyActivity()
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 1) { eventBus.emit(SessionEvent.UserActivity) }
    }

    @Test
    fun `notifyActivity should throttle events`() = testScope.runTest {
        val policy = InteractionPolicy.Timed(throttleIntervalMillis = 1000)
        var currentTime = 1000L
        val interactor = SessionInteractionInteractor(
            testScope, eventBus, policy, logger,
            timeProvider = { currentTime }
        )

        interactor.notifyActivity() // currentTime = 1000 -> Emitted
        testScope.testScheduler.runCurrent()
        
        currentTime = 1500L
        interactor.notifyActivity() // currentTime = 1500 -> Throttled (1500-1000 < 1000)
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 1) { eventBus.emit(SessionEvent.UserActivity) }

        currentTime = 2000L
        interactor.notifyActivity() // currentTime = 2000 -> Emitted (2000-1000 >= 1000)
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 2) { eventBus.emit(SessionEvent.UserActivity) }
    }

    @Test
    fun `notifyActivity should not emit when policy is None`() = testScope.runTest {
        val policy = InteractionPolicy.None
        val interactor = SessionInteractionInteractor(testScope, eventBus, policy, logger)

        interactor.notifyActivity()
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 0) { eventBus.emit(any()) }
    }
}
