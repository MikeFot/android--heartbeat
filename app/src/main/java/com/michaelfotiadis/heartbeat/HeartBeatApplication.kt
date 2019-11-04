package com.michaelfotiadis.heartbeat

import com.michaelfotiadis.heartbeat.core.initialize.AppInitializer
import com.michaelfotiadis.heartbeat.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject

class HeartBeatApplication : DaggerApplication() {

    @Inject
    lateinit var appInitializer: AppInitializer

    override fun onCreate() {
        super.onCreate()
        appInitializer.startup(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
            .application(this)
            .build()
    }
}
