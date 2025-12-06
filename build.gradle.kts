import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

apply { plugin("com.github.ben-manes.versions") }

//Check versions of dependencies: ./gradlew dependencyUpdates -Drevision=milestone -DoutputFormatter=json
//Force-update dependencies:      ./gradlew clean build --refresh-dependencies

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.versionsCheck) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ksp) apply false
}


fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

// Reject all non-stable versions
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
}
