package es.joshluq.authkit.session.model

/**
 * A container for managing authentication tokens such as Access, Refresh, or Custom tokens.
 * Provides utility methods to access, modify, and query the tokens held within a session.
 */
class TokenHolder {

    companion object Defaults {
        /**
         * Creates an empty [TokenHolder].
         *
         * @return A [TokenHolder] with no tokens.
         */
        fun empty(): TokenHolder = TokenHolder()

        /**
         * Creates a [TokenHolder] initialized with a single token.
         *
         * @param token The token to add.
         * @return A [TokenHolder] containing the provided token.
         */
        fun withToken(token: Token): TokenHolder = TokenHolder().apply { addToken(token) }

        /**
         * Creates a [TokenHolder] initialized with multiple tokens.
         *
         * @param tokens The tokens to add.
         * @return A [TokenHolder] containing the provided tokens.
         */
        fun withTokens(vararg tokens: Token): TokenHolder = TokenHolder().apply { tokens.forEach { addToken(it) } }

        /**
         * Creates a [TokenHolder] initialized with a collection of tokens.
         *
         * @param tokens The collection of tokens to add.
         * @return A [TokenHolder] containing the provided tokens.
         */
        fun withTokens(tokens: Collection<Token>): TokenHolder = TokenHolder().apply { tokens.forEach { addToken(it) } }

        /**
         * Creates a [TokenHolder] initialized with a dummy token representing a session without real credentials.
         *
         * @return A [TokenHolder] with a predefined dummy token.
         */
        fun withoutToken(): TokenHolder =
            TokenHolder().apply { addToken(Token.Custom("withoutToken", "withoutToken")) }
    }
    private val tokens: MutableMap<String, Token> = mutableMapOf()

    /**
     * Retrieves all tokens as an immutable map.
     *
     * @return A map of token keys to their corresponding [Token] objects.
     */
    fun getTokens(): Map<String, Token> = tokens.toMap()

    /**
     * Retrieves the Access token if present.
     *
     * @return The [Token.Access] if present, null otherwise.
     */
    fun getAccessToken(): Token.Access? = tokens["access"] as? Token.Access

    /**
     * Retrieves the Refresh token if present.
     *
     * @return The [Token.Refresh] if present, null otherwise.
     */
    fun getRefreshToken(): Token.Refresh? = tokens["refresh"] as? Token.Refresh

    /**
     * Retrieves a Custom token by its specific name.
     *
     * @param name The name of the custom token.
     * @return The [Token.Custom] if present, null otherwise.
     */
    fun getCustomToken(name: String): Token.Custom? = tokens[name] as? Token.Custom

    /**
     * Adds a [Token] to this holder.
     * If a token of the same type already exists, it will be replaced.
     *
     * @param token The token to add.
     */
    fun addToken(token: Token) {
        val key = when (token) {
            is Token.Access -> "access"
            is Token.Refresh -> "refresh"
            is Token.Custom -> token.name
        }
        tokens[key] = token
    }

    /**
     * Checks if this holder contains no tokens.
     *
     * @return True if empty, false otherwise.
     */
    fun isEmpty(): Boolean = tokens.isEmpty()

    /**
     * Checks if an Access token is present.
     *
     * @return True if an Access token exists, false otherwise.
     */
    fun hasAccessToken(): Boolean = tokens.containsKey("access")

    /**
     * Checks if a Refresh token is present.
     *
     * @return True if a Refresh token exists, false otherwise.
     */
    fun hasRefreshToken(): Boolean = tokens.containsKey("refresh")

    /**
     * Checks if a specific Custom token is present.
     *
     * @param name The name of the custom token.
     * @return True if the specified Custom token exists, false otherwise.
     */
    fun hasCustomToken(name: String): Boolean = tokens.containsKey(name)
}
