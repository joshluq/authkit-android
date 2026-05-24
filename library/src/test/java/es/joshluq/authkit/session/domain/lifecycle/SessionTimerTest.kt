package es.joshluq.authkit.session.domain.lifecycle

import es.joshluq.authkit.di.AuthKitLocator
import es.joshluq.authkit.session.sdk.SessionKit
import es.joshluq.foundationkit.log.LoggerKit
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimerTest {

    private val logger: LoggerKit = mockk(relaxed = true)
    private val testScope = TestScope()
    private val sessionKit: SessionKit = mockk(relaxed = true)
    private val timer = SessionTimerImpl(testScope, logger)

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
    fun `timer should notify sessionKit after specified delays`() = testScope.runTest {
        timer.start(durationMillis = 1000, warningThresholdMillis = 400)

        advanceTimeBy(601) // Duration - Warning = 600
        coVerify(exactly = 1) { sessionKit.onPreExpirationDetected() }

        advanceTimeBy(401)
        coVerify(exactly = 1) { sessionKit.onExpirationDetected() }
    }
}
