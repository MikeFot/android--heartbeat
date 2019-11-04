package com.michaelfotiadis.heartbeat.core.leak

import android.app.Application
import javax.inject.Inject

class LeakManager @Inject constructor() {

    @Suppress("UNUSED_PARAMETER", "unused")
    fun initLeakCanary(application: Application): Boolean {
        return true
    }
}
