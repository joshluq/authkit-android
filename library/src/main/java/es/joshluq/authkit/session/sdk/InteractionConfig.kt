package es.joshluq.authkit.session.sdk

/**
 * Configuration for user interaction handling.
 *
 * @property enabled Whether to enable session extension based on user interaction.
 * @property throttleIntervalMillis Minimum time between interaction notifications to avoid overhead.
 */
class InteractionConfig internal constructor(
    val enabled: Boolean,
    val throttleIntervalMillis: Long
)
