package com.kuru.nextgen.component.register

/**
 * This will be leveraged by the feature teams in their projects
 *  to
 *  A) define the entry
 *  B)  behaviour
 *      1) Retry by lifecycle of App
 *      2) Mapping of session/meta data as part of google API to State store
 *      3) UX
 *      4) Post initialization intercepts
 *  c) Pre Condition functions
 *     1)  Compatibility function
 *     2)  Network
 *  d) define the module name
 *  e) B and C are Interceptor, that can be chained function, these functions will return boolean
 *     if any boolean return false , then logs are part of State Store
 *
 */
data class DFModuleDefinitions(
    val moduleName: String,
    val uri: String,
    val entryPointForModule: String,
    val retryCount: Int = 0,
    val deferredDelete: Boolean = false,
    val listOfDFInterceptor: List<DFInterceptor>
)

data class DFInterceptor(val preInstall: Boolean, val task: () -> Boolean)