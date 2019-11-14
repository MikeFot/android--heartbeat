package com.michaelfotiadis.heartbeat.service.di

import android.content.Context
import com.michaelfotiadis.heartbeat.service.BluetoothService
import com.michaelfotiadis.heartbeat.service.ServiceNotificationFactory
import dagger.Module
import dagger.Provides


@Module
internal class BluetoothServiceModule {

    @Provides
    fun provideServiceNotificationFactory(bluetoothService: BluetoothService): ServiceNotificationFactory {
        return ServiceNotificationFactory(bluetoothService)
    }

}