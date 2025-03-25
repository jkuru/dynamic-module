package com.kuru.nextgen.core.feature

import android.app.Application

object ModuleManagerProvider {
    fun getInstance(application: Application): ModuleManager {
        return DynamicFeatureManager.getInstance(application)
    }
} 