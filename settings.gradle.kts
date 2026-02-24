pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Play With AI"

include(":app")
include(":core:theme")
include(":core:ui")
include(":core:auth")
include(":core:config")
include(":core:network")
include(":feature:chat")
include(":feature:map")
