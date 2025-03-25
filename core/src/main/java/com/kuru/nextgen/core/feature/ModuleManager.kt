package com.kuru.nextgen.core.feature

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.Flow

interface ModuleManager {
    fun isModuleInstalled(moduleName: String): Boolean
    fun getModuleState(moduleName: String): Flow<ModuleState>
    suspend fun loadModule(moduleName: String)
    fun retryModuleLoad(moduleName: String)
    fun handleUserConfirmation(moduleName: String)
    fun setConfirmationLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>)
    fun setActivity(activity: Activity?)
    fun cleanup()
}

sealed class ModuleState {
    object NotLoaded : ModuleState()
    object Loading : ModuleState()
    data class LoadingProgress(val progress: Float) : ModuleState()
    object Loaded : ModuleState()
    data class NeedsConfirmation(val confirmationCallback: () -> Unit) : ModuleState()
    data class Error(val message: String) : ModuleState()
} 