package com.michaelfotiadis.heartbeat.ui.main.fragment.info.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.michaelfotiadis.heartbeat.core.livedata.SingleLiveEvent
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeviceInfoViewModel(
    bluetoothRepo: BluetoothRepo,
    private val serviceDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val deviceInfoLiveData = bluetoothRepo.deviceInfoLiveData
    val disconnectLiveData = SingleLiveEvent<Boolean>()

    fun refreshDeviceInfo() {
        viewModelScope.launch {
            serviceDispatcher.refreshDeviceInfo()
        }
    }

    fun disconnectFromDevice() {
        viewModelScope.launch {
            serviceDispatcher.disconnectDevice()
            disconnectLiveData.postValue(true)
        }
    }
}


class DeviceInfoViewModelFactory @Inject constructor(
    private val bluetoothRepo: BluetoothRepo,
    private val serviceDispatcher: BluetoothServiceDispatcher
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeviceInfoViewModel(bluetoothRepo, serviceDispatcher) as T
    }
}


