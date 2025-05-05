# Sample Android Project

## Description

This repository contains a sample Android application built using Kotlin and Jetpack Compose. It demonstrates a multi-module architecture approach, separating features into distinct modules alongside a `core` module for shared components and an `app` module for application entry and assembly. The project specifically showcases the implementation of on-demand Dynamic Feature Modules via Google Play Feature Delivery.

## Features

* **Modular Architecture:** Organizes code into independent feature modules and a shared `core` module.
* **Standard Feature Modules:** Includes standard library modules for:
    * Animals (`:feature-animals`)
    * Cars (`:feature-cars`)
* **Dynamic Feature Module:** Demonstrates on-demand loading with the:
    * Plants (`:feature_plants`) module
* **Core Module:** Contains shared utilities, resources (like navigation routes in `routes.json` [cite: source_file]), and potentially base classes used across features.
* **Application Module:** The main entry point (`app`) that integrates the different feature modules and handles base application setup.

## Dynamic Feature Module Installation

This project demonstrates the use of Android's Dynamic Feature Modules for on-demand delivery, specifically showcased by the `:feature_plants` module. Unlike the `:feature-animals` and `:feature-cars` modules which are standard libraries included in the base install, `:feature_plants` is designed to be downloaded and installed only when needed.

**Implementation Highlights:**

* **Play Feature Delivery:** The `app` module includes the necessary dependencies for Google Play Feature Delivery (`play.feature.delivery` and `play.feature.delivery.ktx`) to manage the download and installation process [cite: source_file].
* **Module Configuration:**
    * The `:feature_plants` module applies the `com.android.dynamic-feature` plugin in its `build.gradle.kts` file [cite: source_file].
    * The `app` module's `build.gradle.kts` declares `:feature_plants` in the `dynamicFeatures` block [cite: source_file].
* **Installation Trigger & Framework:**
    * Navigation to the "Plants" feature within the `MainActivity` initiates the process [cite: source_file].
    * Instead of directly using the Play Core library's `SplitInstallManager` within `MainActivity`, the app launches a specialized `DFComponentActivity` (from the `com.kuru.featureflow.component.ui` package) via an Intent [cite: source_file].
    * This `DFComponentActivity` appears to encapsulate the logic for requesting the module, handling installation states (downloading, installing), and then navigating to the actual feature content (likely `PlantsScreen` within `:feature_plants`). It uses a custom URI (`/chase/df/route/feature_plants`) passed in the Intent to identify the target [cite: source_file].
    * While the core Play Core library dependencies are in `app` [cite: source_file], and the dynamic feature module `:feature_plants` depends on the `:core` module [cite: source_file], the primary framework facilitating the dynamic install flow seems to be this external `DFComponentActivity` / `featureflow` component, which is invoked by the `app` module [cite: source_file]. The `:core` module might provide supporting configurations or utilities (like the navigation routes defined in `routes.json` [cite: source_file]), but the fetched files don't show it directly handling the `SplitInstallManager` API calls.

This approach abstracts the dynamic installation complexity away from the main application flow, potentially allowing for a more standardized way to handle navigation to and loading of dynamic features across the app.

## Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`)
* **Architecture:** Multi-module, MVVM/MVI (inferred), Dynamic Feature Modules
* **Dependency Injection:** Hilt (inferred from plugin usage)
* **Dynamic Delivery:** Play Feature Delivery

## Project Structure

The project follows a multi-module structure:


Sample/
├── app/                 # Main application module, integrates features
├── core/                # Shared code, resources, utilities [cite: source_file]
├── feature-animals/     # Standard library feature module for Animals
├── feature-cars/        # Standard library feature module for Cars
├── feature_plants/      # Dynamic Feature module for Plants
├── gradle/              # Gradle wrapper files
├── build.gradle.kts     # Root build script [cite: source_file]
├── settings.gradle.kts  # Module inclusion settings [cite: source_file]
├── gradle.properties    # Gradle configuration [cite: source_file]
├── local.properties     # Local environment settings (SDK path, etc. - not checked in) [cite: source_file]
└── keystore.properties  # Keystore configuration (sensitive - not checked in) [cite: source_file]


## Setup and Build

1.  **Clone the Repository:** (Assuming this code was hosted)
    ```bash
    git clone <repository-url>
    cd Sample
    ```
2.  **Open in Android Studio:** Open the `Sample` project folder in Android Studio (latest stable version recommended).
3.  **Gradle Sync:** Android Studio should automatically sync the Gradle project. If not, trigger a sync manually (File > Sync Project with Gradle Files).
4.  **Configuration:**
    * Ensure you have the necessary Android SDK installed via the SDK Manager in Android Studio (targetSdk is 34 [cite: source_file]).
    * You might need to create a `local.properties` file in the root directory with the path to your Android SDK, e.g.:
        ```properties
        sdk.dir=/path/to/your/android/sdk
        ```
      [cite: source_file]
    * If the project requires signing for release builds, you will need to create and configure `keystore.properties` with your signing information (this file is typically not included in version control for security) [cite: source_file].
5.  **Build:** Build the project using Android Studio (Build > Make Project) or via the Gradle wrapper:
    ```bash
    ./gradlew build
    ```
6.  **Run:** Run the application on an Android emulator or a physical device using Android Studio (Run > Run 'app').

## Notes

* This README is generated based on the project structure and build configuration files. Specific implementation details within the source code may vary.
* Dependencies and specific library versions can be found in the `build.gradle.kts` files within each module.
* The project utilizes build features like Compose and sets Java compatibility to version 17 [cite: source_file].

