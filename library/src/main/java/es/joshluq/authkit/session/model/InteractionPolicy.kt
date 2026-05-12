package es.joshluq.authkit.session.model

/**
 * Policy for handling user interactions and session extension.
 */
sealed interface InteractionPolicy {
    /**
     * No session extension based on interactions.
     */
    data object None : InteractionPolicy

    /**
     * Session extension enabled with a throttle interval.
     *
     * @property throttleIntervalMillis Minimum time between interaction notifications to avoid overhead.
     */
    data class Timed(val throttleIntervalMillis: Long = 5000L) : InteractionPolicy
}
