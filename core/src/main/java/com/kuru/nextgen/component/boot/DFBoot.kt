package com.kuru.nextgen.component.boot

import com.kuru.nextgen.component.register.DFComponent

/**
 * Dynamic feature Initialize the Dynamic Feature Component , the singleton reference is held
 * at the app level using any DI engine of the App
 *
 *  Who will call the DynamicFeatureComponent to install module ?
 *    Two use case
 *    a) Within App navigation using intent or navigation graph
 *    b) Deep link using intent , recommend using new intent filters pattern
 */
interface DFBoot {

    fun initialize(config: Any) : DFComponent

}