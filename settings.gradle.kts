pluginManagement {
    includeBuild("build-logic")
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
        mavenLocal()
    }
}

rootProject.name = "gptmap"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core:designsystem")
include(":core:common")
include(":core:data")
include(":core:model")
include(":feature:map")
include(":core:domain")
include(":core:chatgpt")
include(":feature:street-view")
include(":feature:login")
include(":feature:register")
include(":core:google-auth")
include(":feature:forgot-password")
include(":core:mongodb")
include(":core:palm")
