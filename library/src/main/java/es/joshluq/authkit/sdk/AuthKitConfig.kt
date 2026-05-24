package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for AuthKit.
 */
class AuthKitConfig internal constructor(
    val context: Context,
    val storeName: String,
    val logger: LoggerKit
) : ManagerConfig
