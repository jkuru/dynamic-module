plugins {
    id("com.android.dynamic-feature")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}


android {
    namespace = "com.kuru.nextgen.plants"
    compileSdk = 34

    defaultConfig {
        minSdk = 31

 //       missingDimensionStrategy("store", "play")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles("proguard-rules.pro")
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }



}

dependencies {
    implementation(project(":app"))
    api(project(":core"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlin.stdlib)
    implementation(libs.featureflow)
    implementation(libs.hilt.android)


    // Use correct aliases from libs.versions.toml
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.runtime) // Managed by BOM
    implementation(libs.androidx.compose.ui)      // Managed by BOM
    implementation(libs.androidx.compose.ui.tooling.preview) // Managed by BOM
    implementation(libs.androidx.compose.material3) // Version specified in TOML (check compatibility if needed)

    implementation(libs.androidx.lifecycle.runtime.ktx) // Use base alias
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Use base alias
    implementation(libs.androidx.navigation.compose) // Use base alias

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.kotlin.stdlib)

    // Play Core dependencies (Assuming these were present before, add if needed)
    // implementation(libs.play.feature.delivery)
    // implementation(libs.play.feature.delivery.ktx)
    // implementation(libs.kotlinx.coroutines.play.services)

    // Other dependencies (Assuming these were present before, add if needed)
    // implementation(libs.androidx.datastore.core.android)
    // implementation(libs.androidx.multidex)
    // implementation(libs.material) // Android Material (View system)

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