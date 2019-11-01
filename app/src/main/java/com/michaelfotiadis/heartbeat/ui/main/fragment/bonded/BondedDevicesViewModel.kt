package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.core.livedata.SingleLiveEvent
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDeviceMapper
import javax.inject.Inject

class BondedDevicesViewModel(
    private val bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher,
    private val uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModel() {

    val devicesLiveData: LiveData<List<UiBondedDevice>> =
        Transformations.map(bluetoothStatusProvider.bondedDevicesLiveData) { bondedDevices ->
            uiBondedDeviceMapper.map(bondedDevices)
        }
    val actionLiveData = SingleLiveEvent<Action>()

    fun refreshBondedDevices() {
        intentDispatcher.refreshBondedDevices()
    }

    fun onDeviceSelected(uiBondedDevice: UiBondedDevice) {
        actionLiveData.postValue(Action.ConnectToDevice(uiBondedDevice))
    }

    fun onMissingDeviceClicked() {
        actionLiveData.postValue(Action.MoveToLocationPermission)
    }
}

sealed class Action {
    object MoveToLocationPermission : Action()
    data class ConnectToDevice(val uiBondedDevice: UiBondedDevice) : Action()
}

class BondedDevicesViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher,
    private val uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BondedDevicesViewModel(
            bluetoothStatusProvider,
            intentDispatcher,
            uiBondedDeviceMapper
        ) as T
    }
}