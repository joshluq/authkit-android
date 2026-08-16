package es.joshluq.authkit.showcase

import android.app.Application
import android.util.Log
import es.joshluq.authkit.network.sdk.NetworkKit
import es.joshluq.authkit.network.sdk.NetworkKitConfig
import es.joshluq.authkit.network.sdk.TokenRefresher
import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.session.model.Token
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.authkit.session.sdk.SessionKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class ShowcaseApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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
            
            // Adding the Network Automation Plugin
            addFeature(NetworkKit, NetworkKitConfig.build {
                tokenRefresher = object : TokenRefresher {
                    override suspend fun refresh(oldTokens: TokenHolder): Result<TokenHolder> {
                        // Mocking an API call to refresh tokens
                        delay(1000.milliseconds)
                        return if (System.currentTimeMillis() % 2 == 0L) {
                            Result.success(TokenHolder().apply {
                                addToken(Token.Access("new_access_token_${System.currentTimeMillis()}"))
                                addToken(Token.Refresh("new_refresh_token"))
                            })
                        } else {
                            Result.failure(Exception("API Error during refresh"))
                        }
                    }
                }
            })
        }

        observeSessionState()
    }

    private fun observeSessionState() {
        applicationScope.launch {
            authKit.session.state.collect { state ->
                Log.d("ShowcaseApp", ">>> [SessionState] $state")
            }
        }
    }
    
    fun isInitialized(): Boolean = this::authKit.isInitialized
}
