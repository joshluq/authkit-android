package es.joshluq.authkit.session.model

/**
 * Defines the persistence strategy for storing session data.
 */
sealed interface PersistencePolicy {
    /**
     * Indicates that session data should only be kept in memory
     * and cleared when the application is terminated.
     */
    data object Transient : PersistencePolicy

    /**
     * Indicates that session data should be persistently stored (e.g., EncryptedSharedPreferences),
     * allowing it to survive application restarts.
     */
    data object Persistent : PersistencePolicy
}
