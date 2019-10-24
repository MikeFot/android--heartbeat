package com.michaelfotiadis.heartbeat.core.initialize

import android.app.Application
import com.facebook.stetho.Stetho
import com.michaelfotiadis.heartbeat.core.features.FeatureFlagProvider
import com.michaelfotiadis.heartbeat.core.leak.LeakManager
import com.michaelfotiadis.heartbeat.core.logger.AppLogger

class AppInitializer(
    private val featureFlagProvider: FeatureFlagProvider,
    private val leakManager: LeakManager,
    private val appLogger: AppLogger
) {

    fun startup(application: Application) {
        if (leakManager.initLeakCanary(application)) {
            appLogger.activate()
            if (featureFlagProvider.isDebugEnabled) {
                Stetho.initializeWithDefaults(application)
                appLogger.get().d("Stethoscope initialized")
            }
        }
    }
}