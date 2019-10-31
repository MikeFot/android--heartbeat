package com.michaelfotiadis.heartbeat.ui.main.di

import com.michaelfotiadis.heartbeat.ui.main.fragment.activation.BluetoothActivationFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.BondedDevicesFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.location.LocationPermissionFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class MainFragmentBuilderModule {

    @ContributesAndroidInjector
    abstract fun bluetoothActivationFragment(): BluetoothActivationFragment

    @ContributesAndroidInjector
    abstract fun bondedDevicesFragment(): BondedDevicesFragment

    @ContributesAndroidInjector
    abstract fun locationPermissionFragment(): LocationPermissionFragment
}
