package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothWrapper
import com.michaelfotiadis.heartbeat.core.livedata.SingleLiveEvent
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDevice
import com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model.UiBondedDeviceMapper
import kotlinx.coroutines.launch
import javax.inject.Inject

class BondedDevicesViewModel(
    private val bluetoothWrapper: BluetoothWrapper,
    private val uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModel() {

    val devicesLiveData = MutableLiveData<List<UiBondedDevice>>()
    val actionLiveData = SingleLiveEvent<Action>()

    fun refreshBondedDevices() {
        viewModelScope.launch {
            val devices = bluetoothWrapper.getBondedDevices()
            val uiDevices = uiBondedDeviceMapper.map(devices)
            devicesLiveData.postValue(uiDevices)
        }
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
    private val bluetoothWrapper: BluetoothWrapper,
    private val uiBondedDeviceMapper: UiBondedDeviceMapper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BondedDevicesViewModel(bluetoothWrapper, uiBondedDeviceMapper) as T
    }
}