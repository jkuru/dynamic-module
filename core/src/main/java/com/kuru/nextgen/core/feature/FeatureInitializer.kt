package com.kuru.nextgen.core.feature

import android.util.Log

// In :app (e.g., com.kuru.nextgen.core.util)
interface FeatureInitializer {
    fun initialize()
}

// In :app (e.g., com.kuru.nextgen.core.util)
object FeatureRegistry {
    private val initializers = mutableListOf<FeatureInitializer>()
    private const val TAG = "DynamicFeatureManager"

    fun registerInitializer(initializer: FeatureInitializer) {
        Log.d(TAG,"FeatureRegistry --> registerInitializer ")
        initializers.add(initializer)
    }

    fun initializeAll() {
        Log.d(TAG,"FeatureRegistry --> initializeAll ")
        initializers.forEach { it.initialize() }
    }
}