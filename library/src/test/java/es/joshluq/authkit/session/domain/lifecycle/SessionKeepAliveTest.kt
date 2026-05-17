package es.joshluq.authkit.session.domain.lifecycle

import es.joshluq.authkit.di.AuthKitLocator
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.foundationkit.log.Loggerkit
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionKeepAliveTest {

    private val logger: Loggerkit = mockk(relaxed = true)
    private val testScope = TestScope()
    private val sessionKit: SessionKit = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(AuthKitLocator)
        io.mockk.every { AuthKitLocator.resolveSessionKit() } returns sessionKit
    }

    @After
    fun tearDown() {
        unmockkObject(AuthKitLocator)
    }

    @Test
    fun `notifyActivity should notify SessionKit when enabled`() = testScope.runTest {
        val policy = InteractionPolicy.Timed(throttleIntervalMillis = 0)
        val keepAlive = SessionKeepAlive(
            testScope, policy, logger,
            timeProvider = { 1000L }
        )

        keepAlive.notifyActivity()
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 1) { sessionKit.onUserActivityDetected() }
    }

    @Test
    fun `notifyActivity should throttle notifications`() = testScope.runTest {
        val policy = InteractionPolicy.Timed(throttleIntervalMillis = 1000)
        var currentTime = 1000L
        val keepAlive = SessionKeepAlive(
            testScope, policy, logger,
            timeProvider = { currentTime }
        )

        keepAlive.notifyActivity() // currentTime = 1000 -> Notified
        testScope.testScheduler.runCurrent()

        currentTime = 1500L
        keepAlive.notifyActivity() // currentTime = 1500 -> Throttled (1500-1000 < 1000)
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 1) { sessionKit.onUserActivityDetected() }

        currentTime = 2000L
        keepAlive.notifyActivity() // currentTime = 2000 -> Notified (2000-1000 >= 1000)
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 2) { sessionKit.onUserActivityDetected() }
    }

    @Test
    fun `notifyActivity should not notify when policy is None`() = testScope.runTest {
        val policy = InteractionPolicy.None
        val keepAlive = SessionKeepAlive(testScope, policy, logger)

        keepAlive.notifyActivity()
        testScope.testScheduler.runCurrent()

        coVerify(exactly = 0) { sessionKit.onUserActivityDetected() }
    }
}
