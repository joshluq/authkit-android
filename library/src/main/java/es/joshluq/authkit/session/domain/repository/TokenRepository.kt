package es.joshluq.authkit.session.domain.repository

import es.joshluq.authkit.session.model.SessionData
import es.joshluq.authkit.session.model.TokenHolder

internal interface TokenRepository {
    suspend fun getTokens(): TokenHolder
    suspend fun saveTokens(tokens: TokenHolder)
    suspend fun <T : SessionData> saveSessionData(data: T, clazz: Class<T>)
    suspend fun <T : SessionData> getSessionData(clazz: Class<T>): T?
    suspend fun clearAll()
}
