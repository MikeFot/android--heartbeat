package com.michaelfotiadis.heartbeat.di

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
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
        appLogger: AppLogger,
        bluetoothAdapter: BluetoothAdapter
    ): BluetoothWrapper {
        return BluetoothWrapper(
            application,
            appLogger,
            bluetoothAdapter
        )
    }

    @Provides
    fun providesBluetoothManager(application: Application): BluetoothManager {
        return application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    @Provides
    fun providesBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter {
        return bluetoothManager.adapter
    }

    @Provides
    @Singleton
    fun providesBluetoothStatusProvider(): BluetoothStatusProvider {
        return BluetoothStatusProvider()
    }
}