package com.kuru.nextgen

import android.app.Application
import android.util.Log
import com.kuru.nextgen.core.feature.DeferredDynamicFeatureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NextGenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        const val PLANTS_MODULE = "feature_plants"
        const  val TAG = "DynamicFeatureManager"
    }
}