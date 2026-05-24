package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for the AuthKit SDK.
 * This class holds the core parameters required for the initialization and operation
 * of the SDK and its dependent plugins.
 *
 * @property context The application context.
 * @property storeName The name of the preferences store to be used.
 * @property encryptionAlias The alias for the encryption key in the Android Keystore.
 * @property logger The logger instance for internal SDK logging.
 */
class AuthKitConfig internal constructor(
    val context: Context,
    val storeName: String,
    val encryptionAlias: String,
    val logger: LoggerKit
) : ManagerConfig
