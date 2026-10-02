---
name: authkit-provider-scaffold
description: Step-by-step procedure to scaffold and implement new authentication providers (Biometrics, Social Login, Passkeys, SSO) into AuthKit following Clean Architecture and the plugin DSL pattern.
---

# AuthKit Provider Scaffolding Skill 🧩

Use this skill when adding a new authentication provider (e.g., Biometrics with `BiometricPrompt`, Google/Social Sign-In, Passkeys with `CredentialManager`, or Custom SSO) into AuthKit.

## Implementation Workflow

### Step 1: Define the Feature Plugin Contract
Every new feature or provider must implement `AuthKitPlugin<TConfig, TInstance>`:

```kotlin
package es.joshluq.authkit.biometric.sdk

import es.joshluq.authkit.sdk.AuthKit
import es.joshluq.authkit.sdk.AuthKitPlugin

object BiometricKit : AuthKitPlugin<BiometricKitConfig, BiometricKitInstance> {
    override fun install(
        authKit: AuthKit,
        config: BiometricKitConfig
    ): BiometricKitInstance {
        // Build internal dependencies using authKit.context and config
        return BiometricKitInstance(config)
    }
}
```

### Step 2: Create the DSL Configuration Builder
Provide a strongly typed configuration builder that fits into `AuthKit.init { addFeature(...) }`:

```kotlin
class BiometricKitConfig internal constructor(
    val title: String,
    val subtitle: String?,
    val allowDeviceCredential: Boolean
) : ManagerConfig {
    class Builder {
        var title: String = "Biometric Authentication"
        var subtitle: String? = null
        var allowDeviceCredential: Boolean = false

        fun build(): BiometricKitConfig = BiometricKitConfig(title, subtitle, allowDeviceCredential)
    }

    companion object {
        inline fun build(block: Builder.() -> Unit): BiometricKitConfig =
            Builder().apply(block).build()
    }
}
```

### Step 3: Follow Clean Architecture Internals
Structure the provider module/package cleanly:
```
authkit/<provider>/
├── data/
│   ├── datasource/        # Third-party SDK wrappers (isolated)
│   └── repository/        # Repository implementations
├── domain/
│   ├── model/             # Domain entities (immutable, agnostic of external SDKs)
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic & authentication workflows
├── di/                    # Internal component wiring
└── sdk/                   # Public-facing plugin, config, and instance interface
```

### Step 4: Hide Implementation Complexity
- Do **not** expose 3rd-party library types (e.g., Google Play Services classes or raw `BiometricPrompt.AuthenticationResult`) directly in the public SDK interface.
- Map external callbacks or exceptions into AuthKit domain results (e.g., `Result<AuthSession, AuthError>`).

### Step 5: Implement Showcase Verification
Always add an interactive demonstration in the `:showcase` module:
1. Update `showcase/src/main/java/.../MyApp.kt` to install the new feature in `AuthKit.init`.
2. Add a new screen or card in the showcase UI to trigger the flow and display the session state.
3. Validate user cancellation, failure, and success flows.
