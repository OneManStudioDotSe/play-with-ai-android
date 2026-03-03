import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import nl.littlerobots.vcu.plugin.VersionCatalogUpdateExtension

// Check versions of dependencies: ./gradlew versionCatalogUpdate
// Preview updates (dry run):      ./gradlew versionCatalogUpdate --interactive
// Force-update dependencies:      ./gradlew clean build --refresh-dependencies

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.versionCatalogUpdate)
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.google.services) apply false
}

configure<VersionCatalogUpdateExtension> {
    sortByKey.set(true)
    keep {
        keepUnusedVersions.set(true)
    }
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        config.setFrom(files("${rootProject.projectDir}/detekt.yml"))

        buildUponDefaultConfig = true
        autoCorrect = true
    }

    configurations.configureEach {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.0")
        }
    }
}
