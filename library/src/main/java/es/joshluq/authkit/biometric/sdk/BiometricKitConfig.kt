package es.joshluq.authkit.biometric.sdk

import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for the [BiometricKit] plugin.
 *
 * @property keyAlias The alias of the biometric hardware-bound key in Android KeyStore.
 * @property invalidatedByEnrollment If true, the key is permanently invalidated when new biometrics are enrolled.
 */
class BiometricKitConfig internal constructor(
    val keyAlias: String,
    val invalidatedByEnrollment: Boolean,
) : ManagerConfig {
    /**
     * Builder for [BiometricKitConfig].
     */
    class Builder {
        /** The alias for the biometric key. Defaults to "authkit_biometric_key". */
        var keyAlias: String = DEFAULT_KEY_ALIAS

        /** Whether the key should be invalidated upon new biometric enrollment. Defaults to true. */
        var invalidatedByEnrollment: Boolean = true

        /**
         * Builds the [BiometricKitConfig] instance.
         */
        fun build(): BiometricKitConfig =
            BiometricKitConfig(
                keyAlias = keyAlias,
                invalidatedByEnrollment = invalidatedByEnrollment,
            )
    }

    companion object {
        const val DEFAULT_KEY_ALIAS = "authkit_biometric_key"

        /**
         * Inline DSL builder for [BiometricKitConfig].
         */
        inline fun build(block: Builder.() -> Unit): BiometricKitConfig = Builder().apply(block).build()
    }
}
