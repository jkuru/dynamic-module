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

rootProject.name = "Sample"
include(":app")
include(":core")
include(":feature-animals")
include(":feature-cars")
include(":feature_plants")

// Configure the dynamic feature module
project(":feature_plants").projectDir = file("feature_plants")
 