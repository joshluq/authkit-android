package es.joshluq.authkit.session.model

sealed interface ExpirationPolicy {
    data object Never : ExpirationPolicy

    data class Timed(val durationMillis: Long, val warningThresholdMillis: Long? = null) : ExpirationPolicy
}
