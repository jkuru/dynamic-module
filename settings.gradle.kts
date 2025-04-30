pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

rootProject.name = "Sample"
include(":app")
include(":core")
include(":feature_plants")
include(":feature-animals")
include(":feature-cars")

// Configure the dynamic feature module
project(":feature_plants").projectDir = file("feature_plants")
 