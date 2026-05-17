# AuthKit Android SDK 🛡️

AuthKit is a robust, secure, and "Zero Friction" authentication and session management SDK for Android. It simplifies the implementation of complex authentication flows, token management, and secure data storage following Clean Architecture and SOLID principles.

## 🚀 Key Features

*   **Modular Architecture**: Plugin-based system. Install only what you need.
*   **Flexible Session Policies**:
    *   **Persistent**: Stays active across app restarts (Social Network style).
    *   **Transient**: Clears automatically when the app process is closed (Banking style).
*   **Expiration Management**: Integrated foreground timers and system-level background alarms (AlarmManager) for precise session timeouts.
*   **Managed Data Storage**: Securely store additional user context (profiles, roles) linked to the session lifecycle.
*   **Network Automation**:
    *   **Interceptor**: Automatic `Authorization: Bearer` header injection.
    *   **Authenticator**: Silent and thread-safe token refresh handling 401 errors.
*   **Memory Safe**: No static context references, preventing memory leaks.
*   **Developer Friendly**: Clean DSL for initialization and clear traceability.

## 📦 Installation

Add the library to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("es.joshluq.authkit:library:1.2.0")
}
```

## 🛠️ Quick Start

### 1. Initialization

Initialize AuthKit in your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        AuthKit.init(this) {
            storeName = "my_secure_store"
            
            // Core Session Management
            addFeature(SessionKit, SessionKitConfig.build {
                persistence = PersistencePolicy.Persistent
                expiration = ExpirationPolicy.Timed(durationMillis = 30 * 60 * 1000)
            })

            // Optional Network Automation
            addFeature(NetworkKit, NetworkKitConfig.build {
                tokenRefresher = MyApiTokenRefresher()
            })
        }
    }
}
```

### 2. Basic Session Operations

```kotlin
val sessionKit = authKit.session

// Start a session
sessionKit.startSession(tokens)

// Check current state
val state = sessionKit.state.value // Active, ExpiringSoon, Idle

// Store custom user data
@Serializable
data class UserProfile(val name: String) : SessionData
sessionKit.saveSessionData(UserProfile("Josh"))

// End session (clears tokens and session data)
sessionKit.endSession()
```

### 3. Connect to OkHttp

```kotlin
val networkKit = authKit.plugin<NetworkKit>()!!

val client = OkHttpClient.Builder()
    .addInterceptor(networkKit.interceptor())
    .authenticator(networkKit.authenticator())
    .build()
```

## 📱 Showcase App

The project includes a `:showcase` module with real-world presets:
*   **Social Network**: Validates persistence after app restarts.
*   **Mobile Banking**: Validates high-security transient sessions and timeouts.
*   **Kiosk Mode**: Demonstrates quick expiration and warnings.

## 🏗️ Architecture

AuthKit is built with modern Android standards:
*   **Mediator Pattern**: `SessionKit` centralizes all state transitions.
*   **Service Locator**: `AuthKitLocator` handles internal dependency injection safely.
*   **Inversion of Control**: Plugins are decoupled via interfaces like `NetworkSessionProvider`.
*   **Kotlin Coroutines & Flow**: For reactive and non-blocking operations.

## 🛡️ Security

*   Uses `SharedPreferences` (ready for `EncryptedSharedPreferences` integration).
*   Atomic token and payload deletion.
*   Protection against infinite 401 loops.
*   Automatic handling of `SCHEDULE_EXACT_ALARM` permissions.

---
Developed with ❤️ by the AuthKit Team.
