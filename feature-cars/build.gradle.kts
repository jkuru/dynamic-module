plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // alias(libs.plugins.compose.compiler) // Removed
    alias(libs.plugins.hilt.android) // Apply Hilt if needed in this feature
    alias(libs.plugins.ksp) // Apply KSP if needed here
}

android {
    namespace = "com.kuru.nextgen.feature.cars"
    compileSdk = 34 // Corrected

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Or true if needed for a feature module
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true // Enable Compose if UI is in this feature
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11" // Corrected version
    }
}

dependencies {
    implementation(project(":core")) // Depends on core module

    // Use correct aliases from libs.versions.toml
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)

    // Hilt (if used in this module)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Compose dependencies (if Compose UI is used in this feature)
    implementation(platform(libs.androidx.compose.bom)) // Apply BOM
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    // implementation(libs.androidx.compose.ui.graphics) // Included via BOM / androidx.compose.ui
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) // Check compatibility

    // Lifecycle & Navigation (if needed in this feature)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit) // Use correct alias
    androidTestImplementation(libs.androidx.test.espresso.core) // Use correct alias
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Use correct alias

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.tooling) // Use correct alias
    debugImplementation(libs.androidx.compose.ui.test.manifest) // Use correct alias
}