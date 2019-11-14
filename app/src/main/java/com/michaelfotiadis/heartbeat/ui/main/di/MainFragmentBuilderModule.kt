package com.michaelfotiadis.heartbeat.ui.main.di

import com.michaelfotiadis.heartbeat.ui.main.fragment.activation.BluetoothActivationFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.BondedDevicesFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.connected.DashboardFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.info.DeviceInfoFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.location.LocationPermissionFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.pair.PairDeviceFragment
import com.michaelfotiadis.heartbeat.ui.main.fragment.scan.ScanDevicesFragment
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

    @ContributesAndroidInjector
    abstract fun pairDeviceFragment(): PairDeviceFragment

    @ContributesAndroidInjector
    abstract fun scanFragment(): ScanDevicesFragment

    @ContributesAndroidInjector
    abstract fun connectedFragment(): DashboardFragment

    @ContributesAndroidInjector
    abstract fun deviceInfoFragment(): DeviceInfoFragment
}
