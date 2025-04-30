plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    // alias(libs.plugins.compose.compiler) // Removed
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kuru.nextgen.core"
    compileSdk = 34 // Corrected

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
        compose = true // Enable Compose if needed in core
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11" // Corrected version
    }
}

// Fully corrected dependencies block for :core
dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat) // Added dependency for AppCompatActivity

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // DataStore
    api(libs.androidx.datastore.preferences)

    // Compose (if UI is used in core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) // Check compatibility
    implementation(libs.androidx.compose.material.icons.extended)

    // Lifecycle & Navigation & Activity (Needed for DFActivity & Compose integration)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity) // Base Activity needed for AppCompatActivity
    implementation(libs.androidx.activity.compose) // For Compose integration
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Play Core (for Dynamic Features)
    // Use 'api' if downstream modules also need direct access, 'implementation' otherwise
    api(libs.play.feature.delivery)
    api(libs.play.feature.delivery.ktx)
    implementation(libs.kotlinx.coroutines.play.services) // For coroutine integration with Play tasks

    // Other utilities used
    implementation(libs.gson)
    api(libs.featureflow) // Keep if used here

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit) // Use correct alias if running Android unit tests
    testImplementation(libs.kotlinx.coroutines.test)

    // AndroidTest dependencies
    androidTestImplementation(libs.androidx.test.ext.junit) // Use correct alias
    androidTestImplementation(libs.androidx.test.espresso.core) // Use correct alias
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}