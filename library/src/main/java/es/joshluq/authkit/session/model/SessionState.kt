package es.joshluq.authkit.session.model

/**
 * Represents the current state of a user session within the AuthKit ecosystem.
 */
sealed interface SessionState {

    /**
     * Indicates that the session is currently active and valid.
     */
    data object Active : SessionState

    /**
     * Indicates that the session is still active but will expire soon,
     * often triggering a warning or prompting for user interaction.
     */
    data object ExpiringSoon : SessionState

    /**
     * Indicates that no active session exists, either because it has expired
     * or the user has logged out.
     */
    data object Idle : SessionState
}
