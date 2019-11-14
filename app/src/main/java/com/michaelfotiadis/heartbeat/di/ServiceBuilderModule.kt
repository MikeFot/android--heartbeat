package com.michaelfotiadis.heartbeat.di

import com.michaelfotiadis.heartbeat.service.BluetoothService
import com.michaelfotiadis.heartbeat.service.di.BluetoothServiceModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector(modules = [BluetoothServiceModule::class])
    abstract fun bluetoothService(): BluetoothService
}
