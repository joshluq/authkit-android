package es.joshluq.authkit.di

import android.content.Context
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.StorageProvider

internal typealias ComponentFactory = (SessionKitConfig, StorageProvider, StorageProvider, Context, Loggerkit) -> SessionKitComponent

object SessionKitDefaults {

    internal val factory =
        { session: SessionKitConfig, persistentStorage: StorageProvider, transientStorage: StorageProvider, context: Context, logger: Loggerkit ->
            SessionKitComponent(
                config = session,
                context = context,
                persistentStorage = persistentStorage,
                transientStorage = transientStorage,
                logger = logger
            )
        }
}
