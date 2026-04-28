package es.joshluq.authkit.session.model

data class TokenHolder(
    private val tokens: MutableMap<String, Token>
) {

    fun addToken(token: Token) {
        val key = when (token) {
            is Token.Access -> "access"
            is Token.Refresh -> "refresh"
            is Token.Custom -> token.name
        }
        tokens[key] = token
    }

    fun isEmpty(): Boolean = tokens.isEmpty()

    fun hasAccessToken(): Boolean = tokens.containsKey("access")

    fun hasRefreshToken(): Boolean = tokens.containsKey("refresh")

    fun hasCustomToken(name: String): Boolean = tokens.containsKey(name)
}
