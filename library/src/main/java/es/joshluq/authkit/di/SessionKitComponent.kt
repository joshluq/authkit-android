package es.joshluq.authkit.di

import android.content.Context
import es.joshluq.authkit.session.data.repository.TokenRepositoryImpl
import es.joshluq.authkit.session.domain.lifecycle.SessionKeepAlive
import es.joshluq.authkit.session.domain.lifecycle.SessionTimerImpl
import es.joshluq.authkit.session.domain.usecase.ClearSessionUseCase
import es.joshluq.authkit.session.domain.usecase.GetSessionDataUseCase
import es.joshluq.authkit.session.domain.usecase.GetTokensUseCase
import es.joshluq.authkit.session.domain.usecase.SaveSessionDataUseCase
import es.joshluq.authkit.session.domain.usecase.SaveTokensUseCase
import es.joshluq.authkit.session.model.PersistencePolicy
import es.joshluq.authkit.session.scheduler.SessionAlarmScheduler
import es.joshluq.authkit.session.sdk.SessionKitConfig
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.getValue

/**
 * Internal Dependency Injection component
 * Following the Internal Dependency Graph pattern.
 */

internal class SessionKitComponent(
    private val config: SessionKitConfig,
    private val context: Context,
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

    val saveSessionDataUseCase by lazy {
        SaveSessionDataUseCase(tokenRepository, logger)
    }

    val getSessionDataUseCase by lazy {
        GetSessionDataUseCase(tokenRepository, logger)
    }

    private val sessionScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    val sessionTimer: es.joshluq.authkit.session.domain.lifecycle.SessionTimer by lazy {
        SessionTimerImpl(sessionScope, logger)
    }

    val keepAlive by lazy {
        SessionKeepAlive(config.interactions, logger)
    }

    val sessionScheduler: es.joshluq.authkit.session.scheduler.SessionScheduler by lazy {
        SessionAlarmScheduler(context)
    }
}
