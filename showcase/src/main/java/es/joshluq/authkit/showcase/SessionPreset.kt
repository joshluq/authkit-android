package es.joshluq.authkit.showcase

import es.joshluq.authkit.session.model.ExpirationPolicy
import es.joshluq.authkit.session.model.InteractionPolicy
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.authkit.session.sdk.SessionKitConfig
import java.util.concurrent.TimeUnit

enum class SessionPreset(
    val title: String,
    val description: String,
    val persistence: PersistencePolicy,
    val expiration: ExpirationPolicy
) {
    SocialNetwork(
        title = "Social Network (Persistent)",
        description = "Sessions persist even after app restarts. No timeout.",
        persistence = PersistencePolicy.Persistent,
        expiration = ExpirationPolicy.Never
    ),
    MobileBanking(
        title = "Mobile Banking (Secure)",
        description = "Transient storage (cleared on app close) with 2-minute timeout.",
        persistence = PersistencePolicy.Transient,
        expiration = ExpirationPolicy.Timed(
            durationMillis = TimeUnit.MINUTES.toMillis(2),
            warningThresholdMillis = TimeUnit.SECONDS.toMillis(30)
        )
    ),
    KioskMode(
        title = "Kiosk / Demo Mode",
        description = "Persistent storage but short 30-second timeout.",
        persistence = PersistencePolicy.Persistent,
        expiration = ExpirationPolicy.Timed(
            durationMillis = TimeUnit.SECONDS.toMillis(30),
            warningThresholdMillis = TimeUnit.SECONDS.toMillis(10)
        )
    );

    fun toConfig(): SessionKitConfig = SessionKitConfig.build {
        persistence = this@SessionPreset.persistence
        expiration = this@SessionPreset.expiration
        interactions = InteractionPolicy.Timed(
            throttleIntervalMillis = TimeUnit.SECONDS.toMillis(2)
        )
    }
}
