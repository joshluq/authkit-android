package es.joshluq.authkit.di

import android.content.Context
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.provider.StorageProvider

internal typealias ComponentFactory = (
    SessionKitConfig,
    StorageProvider,
    StorageProvider,
    Context,
    LoggerKit
) -> SessionKitComponent

object SessionKitDefaults {

    internal val factory: ComponentFactory = { session: SessionKitConfig,
            persistentStorage: StorageProvider,
            transientStorage: StorageProvider,
            context: Context,
            logger: LoggerKit ->
        SessionKitComponent(
            config = session,
            context = context,
            persistentStorage = persistentStorage,
            transientStorage = transientStorage,
            logger = logger
        )
    }
}
