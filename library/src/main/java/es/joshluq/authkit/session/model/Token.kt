package es.joshluq.authkit.session.model

/**
 * Represents a cryptographic token used for authentication and authorization.
 */
sealed interface Token {
    /**
     * The raw string value of the token.
     */
    val value: String

    /**
     * An Access Token typically used for authenticating API requests.
     *
     * @property value The raw string value of the access token.
     */
    data class Access(
        override val value: String,
    ) : Token {
        override fun toString(): String = "Token.Access(value=***REDACTED***)"
    }

    /**
     * A Refresh Token typically used to obtain a new Access Token.
     *
     * @property value The raw string value of the refresh token.
     */
    data class Refresh(
        override val value: String,
    ) : Token {
        override fun toString(): String = "Token.Refresh(value=***REDACTED***)"
    }

    /**
     * A Custom Token that doesn't fit the standard Access/Refresh semantics.
     *
     * @property name The identifier or name of the custom token.
     * @property value The raw string value of the custom token.
     */
    data class Custom(
        val name: String,
        override val value: String,
    ) : Token {
        override fun toString(): String = "Token.Custom(name=$name, value=***REDACTED***)"
    }
}
