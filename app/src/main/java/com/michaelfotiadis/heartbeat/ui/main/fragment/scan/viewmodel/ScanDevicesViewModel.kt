package com.michaelfotiadis.heartbeat.ui.main.fragment.scan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDeviceMapper
import javax.inject.Inject

class ScanDevicesViewModel(
    bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher,
    uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModel() {

    val devicesLiveData: LiveData<List<UiBondedDevice>> =
        Transformations.map(bluetoothStatusProvider.scanStatusLiveData) { scanStatus ->
            when (scanStatus) {
                ScanStatus.Started -> listOf()
                is ScanStatus.Scanning -> uiBondedDeviceMapper.map(scanStatus.bleDevices
                    .map { bleDevice -> bleDevice.device })
                is ScanStatus.Finished -> uiBondedDeviceMapper.map(scanStatus.bleDevices
                    .map { bleDevice -> bleDevice.device })
            }
        }

    val actionLiveData: LiveData<Action> =
        Transformations.map(bluetoothStatusProvider.scanStatusLiveData) { scanStatus ->
            when (scanStatus) {
                is ScanStatus.Scanning -> Action.SCANNING
                is ScanStatus.Finished -> Action.FINISHED
                ScanStatus.Started -> Action.SCANNING
                else -> Action.SCANNING
            }
        }

    fun scanForDevices() {
        intentDispatcher.scanForDevices()
    }

    fun onDeviceSelected(uiBondedDevice: UiBondedDevice) {

    }

}

enum class Action {
    SCANNING,
    FINISHED
}

class ScanDevicesViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher,
    private val uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ScanDevicesViewModel(
            bluetoothStatusProvider, intentDispatcher, uiBondedDeviceMapper
        ) as T
    }
}








