package com.kuru.nextgen

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NextGenApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        const val PLANTS_MODULE = "feature-plants"
    }
}