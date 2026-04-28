package es.joshluq.authkit.session.model

sealed interface PersistencePolicy {
    data object Transient : PersistencePolicy
    data object Persistent : PersistencePolicy
}
