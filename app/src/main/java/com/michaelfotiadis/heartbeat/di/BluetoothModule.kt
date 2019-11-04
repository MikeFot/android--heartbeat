package com.michaelfotiadis.heartbeat.di

import android.app.Application
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothWrapper
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class BluetoothModule {

    @Provides
    @Singleton
    fun providesBluetoothWrapper(
        application: Application,
        appLogger: AppLogger
    ): BluetoothWrapper {
        return BluetoothWrapper(
            application,
            appLogger
        )
    }

    @Provides
    @Singleton
    fun providesBluetoothStatusProvider(): BluetoothStatusProvider {
        return BluetoothStatusProvider()
    }
}
