package es.joshluq.authkit.sdk

import android.content.Context
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.manager.ManagerConfig

/**
 * Configuration for AuthKit.
 */
class AuthKitConfig internal constructor(
    val context: Context,
    val storeName: String,
    val logger: Loggerkit
) : ManagerConfig
