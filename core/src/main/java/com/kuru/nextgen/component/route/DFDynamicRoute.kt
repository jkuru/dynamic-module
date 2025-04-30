package com.kuru.nextgen.component.route

import android.net.Uri

data class DynamicRoute(
    val path: String,
    val target: String,  // e.g., Activity/Fragment class name
    val params: List<String> = emptyList(),
    val wildcard: Boolean = false  // For paths like "/df/people/*"
)


class DynamicLinkRouter(private val routes: List<DynamicRoute>) {

    fun match(uri: Uri): DynamicRoute? {
        val pathSegments = uri.pathSegments
        return routes.firstOrNull { route ->
            when {
                // Exact match (e.g., "/df/plants")
                !route.wildcard && route.path == uri.path -> true

                // Wildcard match (e.g., "/df/people/*")
                route.wildcard && uri.path?.startsWith(
                    route.path.removeSuffix("/*")
                ) == true -> true

                // Parameterized path (e.g., "/df/cars/{id}")
                route.path.contains("{") -> {
                    val templateSegments = route.path.split('/')
                    templateSegments.size == pathSegments.size &&
                            templateSegments.zip(pathSegments).all { (template, segment) ->
                                template == segment || template.startsWith("{")
                            }
                }
                else -> false
            }
        }
    }

    fun extractParams(uri: Uri, route: DynamicRoute): Map<String, String> {
        val params = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { params[it] = uri.getQueryParameter(it) ?: "" }

        // Extract path parameters (e.g., "/df/cars/123" -> "id=123")
        if (route.path.contains("{")) {
            val templateParts = route.path.split('/')
            val pathParts = uri.pathSegments
            templateParts.forEachIndexed { index, part ->
                if (part.startsWith("{")) {
                    val paramName = part.removeSurrounding("{", "}")
                    params[paramName] = pathParts.getOrNull(index) ?: ""
                }
            }
        }
        return params
    }
}