package com.michaelfotiadis.heartbeat.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.michaelfotiadis.heartbeat.core.features.FeatureFlagProvider
import com.michaelfotiadis.heartbeat.core.initialize.AppInitializer
import com.michaelfotiadis.heartbeat.core.leak.LeakManager
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
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
    fun providesLeakManager(): LeakManager {
        return LeakManager()
    }

    @Provides
    @Singleton
    fun providesAppInitializer(
        featureFlagProvider: FeatureFlagProvider,
        leakManager: LeakManager,
        appLogger: AppLogger
    ): AppInitializer {
        return AppInitializer(featureFlagProvider, leakManager, appLogger)
    }

    @Provides
    fun providesGson(): Gson {
        return GsonBuilder().setPrettyPrinting().create()
    }

    @Provides
    fun providesExecutionThreads(): ExecutionThreads {
        return ExecutionThreads()
    }
}
