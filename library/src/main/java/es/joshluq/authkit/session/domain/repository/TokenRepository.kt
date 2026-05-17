package es.joshluq.authkit.session.domain.repository

import es.joshluq.authkit.session.model.SessionData
import es.joshluq.authkit.session.model.TokenHolder

internal interface TokenRepository {
    fun getTokens(): TokenHolder
    fun saveTokens(tokens: TokenHolder)
    fun <T : SessionData> saveSessionData(data: T, clazz: Class<T>)
    fun <T : SessionData> getSessionData(clazz: Class<T>): T?
    fun clearAll()
}
