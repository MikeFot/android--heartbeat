package com.michaelfotiadis.heartbeat.core.initialize

import android.app.Application
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import com.chibatching.kotpref.Kotpref
import com.facebook.stetho.Stetho
import com.michaelfotiadis.heartbeat.core.features.FeatureFlagProvider
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import com.michaelfotiadis.heartbeat.core.notification.NotificationChannelInitializer
import com.michaelfotiadis.heartbeat.core.toast.ToastShower
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "BLE"

class AppInitializer(
    private val featureFlagProvider: FeatureFlagProvider,
    private val channelInitializer: NotificationChannelInitializer,
    private val toastShower: ToastShower,
    private val appLogger: AppLogger,
    private val messageRepo: MessageRepo
) {

    private var isInitialized = AtomicBoolean(false)

    fun startup(application: Application) {

        check(!isInitialized.get()) { "Attempted to initialize app more than once" }
        appLogger.activate()
        channelInitializer.initChannel()
        initKotPref(application)
        initToasty()
        initStetho(application)
        observeMessages()

        if (featureFlagProvider.isDebugEnabled) {
            RxBleClient.updateLogOptions(LogOptions.Builder().setLogLevel(LogConstants.DEBUG).build())
        }

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

    private fun initKotPref(application: Application) {
        Kotpref.init(application)
    }

    private fun observeMessages() {
        messageRepo.messageLiveData.observe(ProcessLifecycleOwner.get(), Observer { message ->
            appLogger.get(TAG).d(message)
        })
        messageRepo.errorLiveData.observe(ProcessLifecycleOwner.get(), Observer { message ->
            appLogger.get(TAG).e(message)
        })
    }
}
