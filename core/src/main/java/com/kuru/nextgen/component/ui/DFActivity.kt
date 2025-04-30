package com.kuru.nextgen.component.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kuru.nextgen.component.route.DynamicLinkRouter
import com.kuru.nextgen.component.route.DynamicRoute

/**
 * Entry Point for Deep Link and Navigation within the APP
 *
 * Responsibility is handle deep link filters for Dynamic Module
 * Navigation within the App (Navigation Jetpack Compose or Legacy)
 *
 *
 *
 */
class DFActivity : AppCompatActivity() {
    private lateinit var router: DynamicLinkRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val routes = loadRoutesFromAssets(this)  // Or use Coroutines/RxJava for remote
        router = DynamicLinkRouter(routes)

        // Handle both deep links and regular navigation
        if (intent?.action == Intent.ACTION_VIEW) {
            handleDeepLink(intent) // Deep link case
        } else {
            handleRegularIntent(intent) // Standard navigation case
        }
    }

    private fun handleRegularIntent(intent: Intent?) {
        // Extract target and params from Intent extras (for non-deep-link navigation)
        val EXTRA_TARGET = ""
        val EXTRA_PARAMS = ""
        val target = intent?.getStringExtra(EXTRA_TARGET) ?: return
        val paramsJson = intent.getStringExtra(EXTRA_PARAMS)
        val params = mapOf<String, String>()

        navigateToScreen(target, params)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent  // Update the current Intent
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val matchedRoute = router.match(uri) ?: run {
            //  showErrorScreen("Route not found")
            return
        }
        val params = router.extractParams(uri, matchedRoute)
        navigateToScreen(matchedRoute.target, params)
    }

    private fun navigateToScreen(target: String, params: Map<String, String>) {
        //TODO this is where the dynamic module will get engaged
    }

    private fun loadRoutesFromAssets(context: Context): List<DynamicRoute> {
        val json = context.assets.open("routes.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<DynamicRoute>>() {}.type
        return Gson().fromJson(json, type)
    }

    suspend fun loadRoutesFromRemote(): List<DynamicRoute> {
        val json = "null" //TODO load from external config
        val type = object : TypeToken<List<DynamicRoute>>() {}.type
        return Gson().fromJson(json, type)
    }
}
