---
name: authkit-session-verification
description: Procedures and verification matrix for AuthKit session lifecycle, token refresh race conditions, foreground timers, AlarmManager background alarms, and persistence policies.
---

# AuthKit Session Verification Skill ⏱️

Use this skill when testing or validating the session management engine, token refresh interceptors, expiration timers, and persistence policies in AuthKit.

## Verification Matrix

### 1. Token Refresh Concurrency (401 Race Condition)
When multiple API requests trigger HTTP 401 simultaneously:
- **Requirement**: `TokenRefresher` must only execute a single refresh request.
- **Verification Strategy**:
  - Launch 5+ parallel coroutines hitting a simulated 401 endpoint.
  - Assert that the backend refresh endpoint is called exactly once.
  - Assert that all 5 requests retry successfully with the new token.

### 2. Foreground Timers vs. Background AlarmManager
AuthKit supports both active foreground expiration and OS-level alarms:
- **Foreground**:
  - Uses `Ticker`/Flow to countdown time left in active session.
  - Emits updates to `SessionState.Active` listeners.
- **Background**:
  - Schedules system `AlarmManager` wakeup (`SessionExpirationReceiver`).
  - Upon alarm trigger, clears session tokens and emits `SessionState.Expired`.
- **Verification Checklist**:
  - Test app backgrounding for `T > durationMillis`.
  - Assert session transitions to `Expired` immediately upon waking or app reopen.

### 3. Persistence Policy Validation
| Policy | Behavior on App Kill | Expected State After Re-launch |
| :--- | :--- | :--- |
| `PersistencePolicy.Persistent` | Stored in `EncryptedSharedPreferences` | `SessionState.Active` (if not expired) |
| `PersistencePolicy.Transient` | Kept in-memory only | `SessionState.Unauthenticated` |

**Verification Steps**:
1. Start session with `Transient` policy.
2. Force-stop the application process (`adb shell am force-stop es.joshluq.authkit.showcase`).
3. Re-launch the showcase app.
4. Verify that the previous session does not persist.

### 4. Reactive State Flow Observability
- Validate that `AuthKit.session.observeState()` emits state transitions cleanly:
  - `Unauthenticated` -> `Authenticating` -> `Active` -> `Refreshing` -> `Expired` / `Revoked`
- Verify that collecting scopes are not leaked and cancel properly on `ViewModel.onCleared()`.
