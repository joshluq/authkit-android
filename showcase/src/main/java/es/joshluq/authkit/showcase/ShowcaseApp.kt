package es.joshluq.authkit.showcase

import android.app.Application
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.sdk.SessionKit

class ShowcaseApp : Application() {

    lateinit var authKit: AuthKit
        private set

    lateinit var configManager: ConfigManager
        private set

    override fun onCreate() {
        super.onCreate()
        configManager = ConfigManager(this)
        initializeAuthKit()
    }

    private fun initializeAuthKit() {
        val preset = configManager.getActivePreset() ?: return
        
        authKit = AuthKit.init(this) {
            storeName = "showcase_auth_store"
            addFeature(SessionKit, preset.toConfig())
        }
    }
    
    fun isInitialized(): Boolean = this::authKit.isInitialized
}
