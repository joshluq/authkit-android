package es.joshluq.authkit.session.storage

/**
 * Agnostic encryption provider to delegate sensitive data handling.
 */
interface EncryptionProvider {
    fun encrypt(key: String, value: String): String
    fun decrypt(key: String, encryptedValue: String): String
}

/**
 * Abstraction for session persistence.
 */
interface SessionStorage {
    /**
     * Persists the given session tokens.
     */
    suspend fun saveTokens(tokens: Map<String, String>)

    /**
     * Retrieves the persisted tokens.
     */
    suspend fun getTokens(): Map<String, String>

    /**
     * Clears all persisted session data.
     */
    suspend fun clear()
}
