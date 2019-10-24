package com.michaelfotiadis.heartbeat.core.leak

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class LeakManager {

    fun initLeakCanary(application: Application): Boolean {
        return if (LeakCanary.isInAnalyzerProcess(application)) {
            false
        } else {
            LeakCanary.install(application)
            true
        }
    }
}