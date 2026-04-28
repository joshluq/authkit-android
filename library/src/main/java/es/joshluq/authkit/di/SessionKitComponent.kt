package es.joshluq.authkit.di

import androidx.work.WorkManager
import es.joshluq.authkit.session.data.repository.TokenRepositoryImpl
import es.joshluq.authkit.session.domain.usecase.ClearSessionUseCase
import es.joshluq.authkit.session.domain.usecase.GetTokensUseCase
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.authkit.session.worker.SessionWorkerManagerImpl
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.StorageProvider

/**
 * Internal Dependency Injection component
 * Following the Internal Dependency Graph pattern.
 */

internal class SessionKitComponent(
    private val config: SessionKitConfig,
    private val workManager: WorkManager,
    private val persistentStorage: StorageProvider,
    private val transientStorage: StorageProvider,
    val logger: Loggerkit
) {

    val tokenRepository by lazy {
        when (config.persistence) {
            PersistencePolicy.Persistent -> TokenRepositoryImpl(persistentStorage, logger)
            PersistencePolicy.Transient -> TokenRepositoryImpl(transientStorage, logger)
        }
    }

    val saveTokensUseCase by lazy {
        SaveTokensUseCase(tokenRepository, logger)
    }

    val getTokensUseCase by lazy {
        GetTokensUseCase(tokenRepository, logger)
    }

    val clearSessionUseCase by lazy {
        ClearSessionUseCase(tokenRepository, logger)
    }

    val sessionWorker by lazy {
        SessionWorkerManagerImpl(workManager)
    }
}
