package es.joshluq.authkit.session.event

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event bus for session-related events.
 * Uses SharedFlow to provide a reactive stream of events.
 */
internal class SessionEventBus {
    private val _events = MutableSharedFlow<SessionEvent>(replay = 1)
    val events: Flow<SessionEvent> = _events.asSharedFlow()

    /**
     * Emits a new event to the bus.
     */
    suspend fun emit(event: SessionEvent) {
        _events.emit(event)
    }
}
