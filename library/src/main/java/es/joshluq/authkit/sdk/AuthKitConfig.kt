package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.encryptionkit.sdk.EncryptionKit
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for the AuthKit SDK.
 * This class holds the core parameters required for the initialization and operation
 * of the SDK and its dependent plugins.
 *
 * @property context The application context.
 * @property storeName The name of the preferences store to be used.
 * @property encryptionKit An optional externally provided [EncryptionKit] instance.
 * @property logger The logger instance for internal SDK logging.
 */
class AuthKitConfig internal constructor(
    val context: Context,
    val storeName: String,
    val encryptionKit: EncryptionKit? = null,
    val logger: LoggerKit
) : ManagerConfig
