import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.pluginkit.android.library)
    alias(libs.plugins.pluginkit.android.hilt)
    alias(libs.plugins.pluginkit.android.work)
    alias(libs.plugins.pluginkit.quality)
    alias(libs.plugins.pluginkit.android.testing)
    alias(libs.plugins.pluginkit.android.publishing)
    alias(libs.plugins.kotlin.serialization)
}

group = providers.gradleProperty("groupId").get()
version = providers.gradleProperty("libraryVersion").get()

configure<LibraryExtension> {
    namespace = "es.joshluq.authkit"
}

dependencies {
    implementation("es.joshluq.kit:foundationkit:1.2.0-SNAPSHOT")
    implementation("es.joshluq.kit:encryptionkit:1.1.0")
    implementation(libs.kotlinx.serialization.json)
}

pluginkitQuality {
    sonarHost = "https://sonarcloud.io"
    sonarProjectKey = "joshluq_authkit-android"
    koverExclusions = listOf(
        "**.showcase.*",
        "**.di.*",
        "**.*_di_*",
        "**.BuildConfig",
        "**.R",
        "**.R$*",
        "**.Dagger*",
        "**.*_Factory",
        "**.*_Factory*",
        "**.*_MembersInjector",
        "**.*_HiltModules*",
        "**.Hilt_*",
        "**.*_Provide*Factory*"
    )
}

androidPublishing {
    repoName = "GitHubPackages"
    repoUrl = "${providers.gradleProperty("repositoryUrl").get()}/${providers.gradleProperty("artifactId").get()}-android"
    repoUser = System.getenv("GITHUB_ACTOR")
    repoPassword = System.getenv("GITHUB_TOKEN")
    version = "${project.version}${project.findProperty("versionType")}"
    groupId = project.group.toString()
    artifactId = providers.gradleProperty("artifactId").get()
}
