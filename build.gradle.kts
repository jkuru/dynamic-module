// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) version "8.5.0" apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
//    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}