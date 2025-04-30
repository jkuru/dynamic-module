package com.kuru.nextgen

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import com.kuru.featureflow.component.googleplay.DFComponentInstaller
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject

@HiltAndroidApp
class NextGenApplication : Application() {

    @Inject
    lateinit var dataStore: DataStore<Preferences> // From AppModule

    @Inject
    lateinit var componentInstaller: DFComponentInstaller // From FrameworkBindingsModule

    @Inject
    lateinit var applicationScope: CoroutineScope // From AppModule

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            val isFeatureInitializedKey = componentInstaller.isComponentInstalled(PLANTS_MODULE)
            Log.d(TAG, "is Feature Installed $isFeatureInitializedKey")
        }
    }

    companion object {
        const val PLANTS_MODULE = "feature-plants"
        const val TAG = "DynamicFeatureManager"
    }
}