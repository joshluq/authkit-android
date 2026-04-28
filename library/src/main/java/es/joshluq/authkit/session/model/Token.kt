package es.joshluq.authkit.session.model

sealed class Token(open val value: String) {
    data class Access(override val value: String) : Token(value)
    data class Refresh(override val value: String) : Token(value)
    data class Custom(val name: String, override val value: String) : Token(value)
}