package es.joshluq.authkit.session.domain.repository

import es.joshluq.authkit.session.model.TokenHolder

internal interface TokenRepository {
    fun getTokens(): TokenHolder
    fun saveTokens(tokens: TokenHolder)
    fun clearAll()
}
