import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kuru.nextgen"
    compileSdk = 34 // Correct

    val versionMajor = 87
    val versionMinor = 1
    val versionPatch = 0

    defaultConfig {
        applicationId = "com.kuru.nextgen"
        minSdk = 31
        targetSdk = 34 // Correct
        versionCode = versionMajor
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // Ensure keystore.properties exists at the root project level
            val propsFile = rootProject.file("keystore.properties")
            if (propsFile.exists()) {
                val props = Properties().apply {
                    load(FileInputStream(propsFile))
                }
                storeFile = file(props["storeFile"] as String)
                storePassword = props["storePassword"] as String
                keyAlias = props["keyAlias"] as String
                keyPassword = props["keyPassword"] as String
            } else {
                println("Warning: keystore.properties not found. Release builds may fail signing.")
                // Optionally configure dummy signing config for debug/dev builds
                // storeFile = file("debug.keystore") // Example
                // storePassword = "android" // Example
                // keyAlias = "androiddebugkey" // Example
                // keyPassword = "android" // Example
            }
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                //    getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        compose = true // Correctly enables compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11" // Corrected version for Kotlin 1.9.23 / BOM 2024.05.00
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


    bundle {
        language {
            enableSplit = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
    }

    // Ensure dynamic feature module name is correct
    dynamicFeatures.add(":feature_plants")
}

// Corrected dependencies block using aliases from the cleaned libs.versions.toml
dependencies {
    implementation(project(":core")) // Project dependency
    implementation(project(":feature-animals")) // Added back from original file
    implementation(project(":feature-cars")) // Added back from original file

    // Use correct aliases from libs.versions.toml
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.runtime) // Managed by BOM
    implementation(libs.androidx.compose.ui)      // Managed by BOM
    implementation(libs.androidx.compose.ui.graphics) // Added back from original file (Managed by BOM)
    implementation(libs.androidx.compose.ui.tooling.preview) // Managed by BOM
    implementation(libs.androidx.compose.material3) // Version specified in TOML
    implementation(libs.androidx.compose.material.icons.extended) // Added back from original file (Managed by BOM)


    implementation(libs.androidx.lifecycle.runtime.ktx) // Use base alias
    implementation(libs.androidx.activity.compose) // Use base alias (Managed by BOM or activity version)
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Use base alias
    implementation(libs.androidx.navigation.compose) // Use base alias

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlin.stdlib)


    // Other dependencies from original file
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.multidex)
    implementation(libs.material) // Alias for older material, check if needed


    // Play Core dependencies
    api(libs.play.feature.delivery) // Use 'api' or 'implementation' as needed
    api(libs.play.feature.delivery.ktx) // Use 'api' or 'implementation' as needed
    implementation(libs.kotlinx.coroutines.play.services)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit) // Use androidx.test.ext alias
    androidTestImplementation(libs.androidx.test.espresso.core) // Use androidx.test alias
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Use BOM for Compose test dependencies
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // Managed by BOM

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.tooling) // Managed by BOM
    debugImplementation(libs.androidx.compose.ui.test.manifest) // Managed by BOM
}