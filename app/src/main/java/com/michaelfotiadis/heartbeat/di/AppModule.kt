package com.michaelfotiadis.heartbeat.di

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.michaelfotiadis.heartbeat.core.features.FeatureFlagProvider
import com.michaelfotiadis.heartbeat.core.initialize.AppInitializer
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import com.michaelfotiadis.heartbeat.core.notification.NotificationChannelInitializer
import com.michaelfotiadis.heartbeat.core.permission.PermissionsHandler
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.core.toast.ToastShower
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class AppModule {

    @Provides
    @Singleton
    fun providesApplicationContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    fun providesResources(application: Application): Resources {
        return application.resources
    }

    @Provides
    fun providesFeatureFlags(): FeatureFlagProvider {
        return FeatureFlagProvider()
    }

    @Provides
    @Singleton
    fun providesAppLogger(featureFlagProvider: FeatureFlagProvider): AppLogger {
        return AppLogger(featureFlagProvider)
    }

    @Provides
    fun providesToastShower(): ToastShower {
        return ToastShower()
    }

    @Provides
    fun providesNotificationChannelInitializer(
        resources: Resources,
        notificationManager: NotificationManager
    ): NotificationChannelInitializer {
        return NotificationChannelInitializer(resources, notificationManager)
    }

    @Provides
    @Singleton
    fun providesAppInitializer(
        featureFlagProvider: FeatureFlagProvider,
        channelInitializer: NotificationChannelInitializer,
        toastShower: ToastShower,
        appLogger: AppLogger
    ): AppInitializer {
        return AppInitializer(
            featureFlagProvider,
            channelInitializer,
            toastShower,
            appLogger
        )
    }

    @Provides
    fun providesPermissionsHandler(): PermissionsHandler {
        return PermissionsHandler()
    }

    @Provides
    fun providesGson(): Gson {
        return GsonBuilder().setPrettyPrinting().create()
    }

    @Provides
    fun providesExecutionThreads(): ExecutionThreads {
        return ExecutionThreads()
    }

    @Provides
    fun providesNotificationManager(context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun providesServiceDispatcher(context: Context): BluetoothServiceDispatcher {
        return BluetoothServiceDispatcher(context)
    }
}
