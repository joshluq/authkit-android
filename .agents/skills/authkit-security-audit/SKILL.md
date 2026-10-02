---
name: authkit-security-audit
description: Audits AuthKit Android code for security compliance, credential and token leak prevention, secure storage (Keystore, EncryptedSharedPreferences), and memory hygiene.
---

# AuthKit Security Audit Skill 🛡️

Use this skill when reviewing code changes, pull requests, refactoring authentication flows, or before preparing a release of the AuthKit SDK.

## Security Audit Checklist

### 1. Zero Token & Credential Leaks in Public APIs
- [ ] **No Raw Tokens in Public Models**: Ensure public data structures (like `SessionState` or public interfaces) do not expose raw JWT strings, secret keys, or cryptographic materials.
- [ ] **Safe Accessors**: Access to tokens should be encapsulated behind authenticated actions (e.g., automated OkHttp interceptor/authenticator) or secure provider callbacks.
- [ ] **Method Signatures**: Public functions must not accept or return sensitive credentials as unencrypted `String` when alternatives (such as secure token containers or `CharArray`) are feasible.

### 2. Secure Local Storage
- [ ] **Encryption Required**: All persistent session artifacts must use `EncryptionKit` or `EncryptedSharedPreferences` backed by the Android Keystore.
- [ ] **No Plain SharedPreferences**: Verify that standard `SharedPreferences` or unencrypted databases are never used to store tokens, refresh tokens, user identifiers, or security keys.
- [ ] **Process Death Handling**: Verify that `PersistencePolicy.Transient` properly purges tokens upon process termination and never writes to disk.

### 3. Logging & Obfuscation Hygiene
- [ ] **Redacted Logs**: Inspect all calls to `LoggerKit`, `Log.*`, or `println`. Ensure tokens, user credentials, passwords, and PII are redacted or omitted.
- [ ] **Exceptions**: Ensure exception messages and stack traces never concatenate sensitive user data or authentication headers.
- [ ] **ProGuard / R8 Rules**: Check that consumer ProGuard rules (`consumer-rules.pro`) preserve internal security contracts while stripping debug logging in release builds.

### 4. Memory Hygiene & Leak Prevention
- [ ] **Context Leaks**: Never store static references to `Activity` or UI `Context`. Only `applicationContext` may be retained within singletons or components (`AuthKit.context`).
- [ ] **Memory Zeroing**: Sensitive char arrays or cryptographic byte arrays should be cleared (`Arrays.fill(..., 0.toByte())`) when no longer needed.
- [ ] **Deregistration**: Ensure listeners, receivers (`SessionExpirationReceiver`), and coroutine scopes are cancelled or unregistered when sessions are cleared.

### 5. Network Security & Token Refresh
- [ ] **Bearer Formatting**: Ensure the `Authorization` header follows strict RFC 6750 standards: `Authorization: Bearer <token>`.
- [ ] **Thread-Safe Refresh**: Ensure silent token refresh (`TokenRefresher`) uses synchronization (e.g., Mutex) to prevent concurrent refresh storms upon receiving multiple 401 responses.
- [ ] **Infinite Loop Prevention**: Ensure 401 retry loops abort after a single failed refresh attempt to prevent infinite retry cascades.
