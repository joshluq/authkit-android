package es.joshluq.authkit.session.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.joshluq.authkit.session.SessionManager
import es.joshluq.authkit.session.SessionManagerImpl
import es.joshluq.authkit.session.storage.SessionStorage
import es.joshluq.authkit.session.storage.SessionStorageImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun bindSessionStorage(impl: SessionStorageImpl): SessionStorage

    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl<Any>): SessionManager<Any>

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}
