package es.joshluq.authkit.session.storage

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit

/**
 * Implementation of [SessionStorage] that persists tokens in SharedPreferences.
 * Sensitive data is encrypted via the provided [EncryptionProvider].
 */
class SessionStorageImpl @Inject constructor(
    context: Context,
    private val encryptionProvider: EncryptionProvider
) : SessionStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun saveTokens(tokens: Map<String, String>) {
        prefs.edit {
            tokens.forEach { (key, value) ->
                val encryptedValue = encryptionProvider.encrypt(key, value)
                putString(key, encryptedValue)
            }
        }
    }

    override suspend fun getTokens(): Map<String, String> {
        val tokens = mutableMapOf<String, String>()
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                try {
                    val decryptedValue = encryptionProvider.decrypt(key, value)
                    tokens[key] = decryptedValue
                } catch (e: Exception) {
                    // Log or handle decryption error
                }
            }
        }
        return tokens
    }

    override suspend fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "es.joshluq.authkit.session_prefs"
    }
}
