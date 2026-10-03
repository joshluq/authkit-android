# AuthKit Android SDK 🛡️

AuthKit is a robust, secure, and "Zero Friction" authentication and session management SDK for Android. It simplifies the implementation of complex authentication flows, token management, hardware-backed biometric integration, and secure data storage following Clean Architecture and SOLID principles.

Powered by `EncryptionKit` and `FoundationKit`.

---

## 🚀 Key Features

*   **Modular Architecture**: Plugin-based system (`AuthKitPlugin`). Install only what you need.
*   **Hardware-Backed Biometrics (`BiometricKit`)**: Seamless integration with `BiometricPrompt` using Android KeyStore-generated `CryptoObject` (AES/GCM/NoPadding) with automatic key invalidation on new biometric enrollment.
*   **Zero-Downtime KeyStore Key Rotation**: Rotate master encryption keys on the fly (`rotateSessionStore()`). Data remains transparently readable using historic keys while new writes use the freshly rotated key.
*   **RFC 9449 DPoP (Device-Bound Proof-of-Possession)**: Optional cryptographic binding of access tokens to the physical device. The OkHttp interceptor automatically generates and attaches signed `DPoP` proof headers to prevent token replay attacks.
*   **In-Memory Hygiene (`SecureBytes`)**: RAM zeroization of sensitive credentials and tokens immediately after serialization/deserialization.
*   **Flexible Session Policies**:
    *   **Persistent**: Stays active across app restarts (Social Network style).
    *   **Transient**: Clears automatically when the app process is closed (Banking style).
*   **Expiration Management**: Integrated foreground tickers and system-level background alarms (`AlarmManager`) for exact session timeouts.
*   **Managed Data Storage**: Securely store typed user context (profiles, roles) linked directly to the session lifecycle.
*   **Network Automation**:
    *   **Interceptor**: Automatic `Authorization: Bearer` and optional `DPoP` header injection.
    *   **Authenticator**: Silent and thread-safe token refresh handling HTTP 401 errors without infinite loops.
*   **Memory Safe**: No static context references, preventing memory leaks.
*   **Developer Friendly**: Clean DSL for initialization and clear traceability.

---

## 📦 Installation

Add AuthKit to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("es.joshluq.authkit:library:1.3.0")

    // Optional: Only required if using BiometricKit
    implementation("androidx.biometric:biometric:1.1.0")
}
```

---

## 🛠️ Quick Start

### 1. Initialization

Initialize AuthKit in your `Application` class using the DSL:

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

            // Optional: Biometric Hardware Authentication
            addFeature(BiometricKit, BiometricKitConfig.build {
                keyAlias = "my_biometric_key"
                invalidatedByEnrollment = true // Invalidates key if user registers new fingerprints
            })

            // Optional: Network Automation & RFC 9449 DPoP
            addFeature(NetworkKit, NetworkKitConfig.build {
                enableDPoP = true // Opt-in: Attaches signed DPoP proof headers to requests
                tokenRefresher = MyApiTokenRefresher()
            })
        }
    }
}
```

---

### 2. Session Management

Access the session plugin via `authKit.session`:

```kotlin
val sessionKit = authKit.session

// 1. Start a session with tokens
val tokens = TokenHolder().apply {
    addToken(Token.Access("jwt_access_token"))
    addToken(Token.Refresh("jwt_refresh_token"))
}
sessionKit.startSession(tokens)

// 2. Observe session state (StateFlow)
lifecycleScope.launch {
    sessionKit.state.collect { state ->
        when (state) {
            is SessionState.Active -> navigateToHome()
            is SessionState.ExpiringSoon -> showSessionWarningDialog()
            is SessionState.Idle -> navigateToLogin()
            is SessionState.Initializing -> showSplashScreen()
        }
    }
}

// 3. Store custom session data securely
@Serializable
data class UserProfile(val name: String, val email: String) : SessionData

sessionKit.saveSessionData(UserProfile("Josh Luq", "josh@example.com"))
val profile = sessionKit.getSessionData<UserProfile>()

// 4. Extend or End Session
sessionKit.extendSession(newTokens)
sessionKit.endSession()
```

---

### 3. Biometric Hardware Authentication (`BiometricKit`)

Use `authKit.biometric` to integrate with Android's `BiometricPrompt`:

```kotlin
val biometricKit = authKit.biometric

// 1. Create a KeyStore-backed CryptoObject for encryption
val cryptoObject = biometricKit.createEncryptCryptoObject()

// 2. Launch Android BiometricPrompt
val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        val authenticatedCryptoObject = result.cryptoObject ?: return
        
        // 3. Encrypt sensitive payload using the authenticated CryptoObject
        val secretPin = "1234".toByteArray()
        val encryptResult = biometricKit.encrypt(authenticatedCryptoObject, secretPin)
        
        encryptResult.onSuccess { cryptoResult ->
            val ciphertext = cryptoResult.ciphertext
            val iv = cryptoResult.iv
            // Save ciphertext & IV securely
        }
    }
})

val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Unlock App")
    .setSubtitle("Confirm your fingerprint")
    .setNegativeButtonText("Cancel")
    .build()

prompt.authenticate(promptInfo, cryptoObject)

// 4. Decrypt payload on subsequent logins
val decryptCryptoObject = biometricKit.createDecryptCryptoObject(iv = savedIv)
// After authentication succeeds:
val decryptedResult = biometricKit.decrypt(authenticatedCryptoObject, savedCiphertext)
val originalPin = decryptedResult.getOrNull()?.let { String(it) }
```

---

### 4. Zero-Downtime Session Store Key Rotation

Rotate your session store encryption key on schedule or after a security event without logging users out:

```kotlin
lifecycleScope.launch {
    val rotationResult = authKit.rotateSessionStore()
    
    rotationResult.onSuccess {
        // Active encryption key is rotated in Android KeyStore!
        // Existing session data remains readable via historic keys.
    }.onFailure { error ->
        Log.e("Security", "Key rotation failed", error)
    }
}
```

---

### 5. Connect to OkHttp with DPoP (RFC 9449)

Plug `NetworkKit` into your OkHttp client to get automatic bearer injection, silent refresh, and optional DPoP proof signing:

```kotlin
val networkKit = authKit.plugin<NetworkKit>()!!

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(networkKit.interceptor())     // Adds 'Authorization: Bearer' & 'DPoP' header
    .authenticator(networkKit.authenticator())     // Handles 401s silently via TokenRefresher
    .build()
```

When `enableDPoP = true`, every outgoing request automatically contains:
```http
Authorization: Bearer <access_token>
DPoP: <base64url_hardware_signed_proof>
```

---

## 📱 Showcase App

The project includes a `:showcase` module with real-world presets and interactive testing:
*   **Social Network**: Validates session persistence after app restarts.
*   **Mobile Banking**: Validates high-security transient sessions, fast timeouts, and keep-alive resets.
*   **Advanced Security & Hardware Binding Card**:
    *   Live testing of KeyStore key rotation with zero downtime.
    *   Live testing of Biometric KeyStore `CryptoObject` initialization.

To run the showcase:
```bash
./gradlew :showcase:installDebug
```

---

## 🏗️ Architecture & Guiding Principles

AuthKit is built following Clean Architecture and modern Android standards:
*   **Mediator Pattern**: `SessionKit` centralizes and coordinates all session state transitions.
*   **Plugin DSL**: Modular capabilities (`SessionKit`, `BiometricKit`, `NetworkKit`) decouple dependencies and keep the SDK lightweight.
*   **Service Locator**: `AuthKitLocator` securely resolves internal kit references without tight coupling.
*   **Reactive Flow**: State exposure through Kotlin `StateFlow` ensures seamless Jetpack Compose and Coroutines integration.

---

## 🛡️ Security Highlights

*   **Android KeyStore**: Keys are created and retained directly inside hardware-backed storage (StrongBox or TEE where available).
*   **Zero-Downtime Key Migration**: Multi-key derivation allows rotating the primary key while preserving decryption capability for previously written data.
*   **Memory Hygiene**: Critical secrets and tokens are wrapped in `SecureBytes`, which automatically wipes (`Arrays.fill(data, 0)`) RAM content when closed.
*   **Token Replay Protection**: RFC 9449 DPoP binds access tokens to device-generated asymmetric hardware signatures.
*   **Crash & Infinite Loop Resilience**: The OkHttp authenticator caps retries to prevent recursive refresh loops.

---

## 📄 License

```text
Copyright 2026 AuthKit Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
