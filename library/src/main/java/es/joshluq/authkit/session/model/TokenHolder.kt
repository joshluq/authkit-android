package es.joshluq.authkit.session.model

class TokenHolder {

    companion object Defaults {
        fun empty(): TokenHolder = TokenHolder()

        fun withToken(token: Token): TokenHolder = TokenHolder().apply { addToken(token) }

        fun withTokens(vararg tokens: Token): TokenHolder = TokenHolder().apply { tokens.forEach { addToken(it) } }

        fun withTokens(tokens: Collection<Token>): TokenHolder = TokenHolder().apply { tokens.forEach { addToken(it) } }

        fun withoutToken(): TokenHolder =
            TokenHolder().apply { addToken(Token.Custom("withoutToken", "withoutToken")) }
    }
    private val tokens: MutableMap<String, Token> = mutableMapOf()

    fun getTokens() = tokens.toMap()

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
