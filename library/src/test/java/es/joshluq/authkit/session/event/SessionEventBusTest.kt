package es.joshluq.authkit.session.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionEventBusTest {

    @Test
    fun `emit should deliver event to collectors`() = runTest {
        val bus = SessionEventBus()
        val expectedEvent = SessionEvent.Expiration
        
        bus.emit(expectedEvent)
        
        assertEquals(expectedEvent, bus.events.first())
    }
}
