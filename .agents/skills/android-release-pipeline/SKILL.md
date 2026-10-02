---
name: android-release-pipeline
description: Runbook for library versioning, static code analysis (Detekt), unit tests, Fastlane publishing to GitHub Packages, and release documentation.
---

# Android Release Pipeline Skill 🚀

Use this skill when preparing, testing, or releasing a new version of the AuthKit Android SDK to GitHub Packages.

## Release Process

### Step 1: Pre-Release Quality Checks
Run static analysis and test suites locally before bumping versions:

```powershell
# Run Detekt static analysis
.\gradlew.bat detekt

# Run unit tests across all modules
.\gradlew.bat testDebugUnitTest
```

### Step 2: Version Management
1. Verify the release version in `gradle.properties` or the version catalog:
   - `libraryVersion=X.Y.Z`
2. Update [README.md](file:///c:/Users/josh_/AndroidStudioProjects/authkit-android/README.md) installation block with the target version:
   ```kotlin
   dependencies {
       implementation("es.joshluq.authkit:library:X.Y.Z")
   }
   ```
3. Update showcase module dependency if testing local binary compatibility.

### Step 3: Fastlane Publishing
Ensure credentials (`GITHUB_ACTOR` and `GITHUB_TOKEN`) are set in your environment:

#### For Snapshot Releases:
```bash
bundle exec fastlane publish_snapshot
```
*Appends `-SNAPSHOT` to the version.*

#### For Stable Releases:
```bash
bundle exec fastlane publish_release
```

### Step 4: Verification of Published Artifact
1. Check GitHub Packages registry under the repository to confirm the new version is available.
2. In the `:showcase` app or a sample project, resolve the remote dependency to verify artifact integrity and pom file resolution.
