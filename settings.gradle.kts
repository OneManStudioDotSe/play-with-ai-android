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
include(":data:plan")
include(":data:explore")
include(":data:chat")
include(":data:dream")
include(":feature:plan")
include(":feature:chat")
include(":feature:dream")
include(":feature:explore")
