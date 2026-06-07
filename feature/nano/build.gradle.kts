import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "se.onemanstudio.playaroundwithai.feature.nano"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }
    }
}

dependencies {
    implementation(project(":ui:components"))

    implementation(libs.bundles.compose.feature)

    // On-device GenAI (Gemini Nano) availability + download
    implementation(libs.mlkit.genai.summarization)
    // ML Kit GenAI returns Guava ListenableFutures; await() bridges them to coroutines
    implementation(libs.kotlinx.coroutines.guava)

    implementation(libs.timber)

    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.ui.tooling)
}
