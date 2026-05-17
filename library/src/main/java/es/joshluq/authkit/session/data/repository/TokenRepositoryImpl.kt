package es.joshluq.authkit.session.data.repository

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.SessionData
import es.joshluq.authkit.session.model.Token
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.provider.read
import es.joshluq.foundationkit.provider.save
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal class TokenRepositoryImpl(
    private val storage: StorageProvider,
    private val logger: Loggerkit
) : TokenRepository {

    companion object {
        private const val TAG = "TokenRepository"
        private const val TOKENS_KEY = "TOKENS_KEY"
        private const val SESSION_DATA_KEY = "SESSION_DATA_KEY"
    }

    override fun getTokens(): TokenHolder {
        logger.d(TAG, "Reading tokens from storage")
        val jsonTokens: JsonObject = requireNotNull(storage.read(TOKENS_KEY)) { "No tokens found in storage" }
        return TokenHolder().apply {
            jsonTokens.forEach { (key, value) ->
                val token = when (key) {
                    "access" -> Token.Access(value.jsonPrimitive.content)
                    "refresh" -> Token.Refresh(value.jsonPrimitive.content)
                    else -> Token.Custom(key, value.jsonPrimitive.content)
                }
                addToken(token)
            }
        }
    }

    override fun saveTokens(tokens: TokenHolder) {
        logger.d(TAG, "Saving tokens to storage")
        val jsonToken = buildJsonObject {
            tokens.getTokens().forEach { (key, token) ->
                put(key, token.value)
            }
        }
        storage.save(TOKENS_KEY, jsonToken)
    }

    override fun <T : SessionData> saveSessionData(data: T, clazz: Class<T>) {
        logger.d(TAG, "Saving session data: ${clazz.simpleName}")
        storage.save(SESSION_DATA_KEY, data, clazz)
    }

    override fun <T : SessionData> getSessionData(clazz: Class<T>): T? {
        logger.d(TAG, "Reading session data: ${clazz.simpleName}")
        return storage.read(SESSION_DATA_KEY, clazz)
    }

    override fun clearAll() {
        logger.d(TAG, "Clearing all tokens and session data")
        storage.clear()
    }
}
