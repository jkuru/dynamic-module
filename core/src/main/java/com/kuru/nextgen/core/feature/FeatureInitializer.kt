package com.kuru.nextgen.core.feature

import android.util.Log

// In :app (e.g., com.kuru.nextgen.core.util)
interface FeatureInitializer {
    fun initialize()
}

// In :app (e.g., com.kuru.nextgen.core.util)
object FeatureRegistry {
    private val initializers = mutableListOf<FeatureInitializer>()

    fun registerInitializer(initializer: FeatureInitializer) {
        Log.d("FeatureRegistry",initializer.toString())
        initializers.add(initializer)
    }

    fun initializeAll() {
        initializers.forEach { it.initialize() }
    }
}