package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.core.livedata.SingleLiveEvent
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDeviceMapper
import javax.inject.Inject

class BondedDevicesViewModel(
    bluetoothStatusProvider: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher,
    uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModel() {

    val devicesLiveData: LiveData<List<UiBondedDevice>> =
        Transformations.switchMap(
            bluetoothStatusProvider.bondedDevicesLiveData
        ) { set ->
            MutableLiveData<List<UiBondedDevice>>().apply {
                postValue(uiBondedDeviceMapper.map(set))
            }
        }
    val actionLiveData = SingleLiveEvent<Action>()

    fun refreshBondedDevices() {
        intentDispatcher.refreshBondedDevices()
    }

    fun onDeviceSelected(uiBondedDevice: UiBondedDevice) {
        actionLiveData.postValue(
            Action.ConnectToDevice(
                uiBondedDevice
            )
        )
    }

    fun onMissingDeviceClicked() {
        actionLiveData.postValue(Action.MoveToLocationPermission)
    }

    fun disconnectDevice() {
        intentDispatcher.disconnectDevice()
    }
}

sealed class Action {
    object MoveToLocationPermission : Action()
    data class ConnectToDevice(val uiBondedDevice: UiBondedDevice) : Action()
}

class BondedDevicesViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothRepo,
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
