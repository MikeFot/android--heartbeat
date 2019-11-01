package com.michaelfotiadis.heartbeat.core.initialize

import android.app.Application
import com.facebook.stetho.Stetho
import com.michaelfotiadis.heartbeat.core.features.FeatureFlagProvider
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import com.michaelfotiadis.heartbeat.core.notification.NotificationChannelInitializer
import com.michaelfotiadis.heartbeat.core.toast.ToastShower
import java.util.concurrent.atomic.AtomicBoolean

class AppInitializer(
    private val featureFlagProvider: FeatureFlagProvider,
    private val channelInitializer: NotificationChannelInitializer,
    private val toastShower: ToastShower,
    private val appLogger: AppLogger
) {

    private var isInitialized = AtomicBoolean(false)

    fun startup(application: Application) {

        check(!isInitialized.get()) { "Attempted to initialize app more than once" }
        appLogger.activate()
        channelInitializer.initChannel()
        initToasty()
        initStetho(application)
        isInitialized.set(true)
    }

    private fun initStetho(application: Application) {
        if (featureFlagProvider.isDebugEnabled) {
            Stetho.initializeWithDefaults(application)
            appLogger.get().d("Stetho initialized")
        }
    }

    private fun initToasty() {
        toastShower.initialize()
        appLogger.get().d("Toasty initialized")
    }

}