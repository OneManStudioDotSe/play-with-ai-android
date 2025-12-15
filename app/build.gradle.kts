import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maps.secrets)
}

// Read the API key from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
} else {
    // Create a placeholder for CI environments
    localProperties.setProperty("GEMINI_API_KEY_DEBUG", "")
    localProperties.setProperty("GEMINI_API_KEY_RELEASE", "")
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
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY_DEBUG")}\"")
            buildConfigField("String", "BASE_URL", "\"https://generativelanguage.googleapis.com/\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY_RELEASE")}\"")
            buildConfigField("String", "BASE_URL", "\"https://generativelanguage.googleapis.com/\"")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
}

dependencies {
    implementation(project(":core-data"))
    implementation(project(":core-theme"))
    implementation(project(":core-ui"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:map"))

    implementation(libs.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.timber)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.ui.tooling)
}
