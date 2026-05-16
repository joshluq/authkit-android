package es.joshluq.authkit.session.domain.lifecycle

import es.joshluq.authkit.session.domain.timer.SessionTimerImpl
import es.joshluq.authkit.session.event.SessionEvent
import es.joshluq.authkit.session.event.SessionEventBus
import es.joshluq.foundationkit.log.Loggerkit
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTimerTest {

    private val eventBus = SessionEventBus()
    private val logger: Loggerkit = mockk(relaxed = true)
    private val testScope = TestScope()
    private val timer = SessionTimerImpl(testScope, eventBus, logger)

    @Test
    fun `timer should emit events after specified delays`() = testScope.runTest {
        val events = mutableListOf<SessionEvent>()
        val job = launch {
            eventBus.events.take(2).toList(events)
        }

        timer.start(durationMillis = 1000, warningThresholdMillis = 400)

        advanceTimeBy(601) // Duration - Warning = 600
        Assert.assertEquals(1, events.size)
        Assert.assertEquals(SessionEvent.PreExpiration, events[0])

        advanceTimeBy(401)
        Assert.assertEquals(2, events.size)
        Assert.assertEquals(SessionEvent.Expiration, events[1])

        job.cancel()
    }
}