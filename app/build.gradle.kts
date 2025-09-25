import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

// Read the API key from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "se.onemanstudio.playaroundwithai"
    compileSdk = 36

    defaultConfig {
        applicationId = "se.onemanstudio.playaroundwithai"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
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

    buildFeatures {
        compose = true
        buildConfig = true // Enable BuildConfig generation
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    kapt {
        correctErrorTypes = true
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Core stuff
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity)
    implementation(libs.androidx.ui.base)
    implementation(libs.androidx.ui.graphics)

    // Navigation
    implementation(libs.androidx.navigation)

    // ViewModels + coroutines
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose UI
    implementation(libs.ui)
    implementation(libs.ui.preview)

    // Material 3 + friends
    implementation(libs.material3)

    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material.icons.extended)

    // Permissions
    implementation(libs.accompanist.permissions)

    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.material3)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)


    debugImplementation(libs.androidx.ui.tooling)
}
