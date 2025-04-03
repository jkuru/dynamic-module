package com.kuru.nextgen

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.kuru.nextgen.core.feature.DeferredDynamicFeatureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NextGenApplication : Application() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(
            "app_prefs",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate() {
        super.onCreate()
        if (prefs.getBoolean("isFeatureInitialized", false)) {
            prefs.edit().putBoolean("isFeatureInitialized", false).apply()
        }
    }

    companion object {
        const val PLANTS_MODULE = "feature_plants"
        const val TAG = "DynamicFeatureManager"
    }
}