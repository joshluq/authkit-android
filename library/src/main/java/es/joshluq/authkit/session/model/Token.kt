package es.joshluq.authkit.session.model

sealed class Token {
    abstract val value: String

    data class Access(override val value: String) : Token()

    data class Refresh(override val value: String) : Token()

    data class Custom(val name: String, override val value: String) : Token()
}
