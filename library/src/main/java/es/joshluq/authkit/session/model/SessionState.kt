package es.joshluq.authkit.session.model

/**
 * Represents the current state of a user session.
 */
sealed interface SessionState {

    data object Active : SessionState

    data object ExpiringSoon : SessionState


    data object Idle : SessionState

}
