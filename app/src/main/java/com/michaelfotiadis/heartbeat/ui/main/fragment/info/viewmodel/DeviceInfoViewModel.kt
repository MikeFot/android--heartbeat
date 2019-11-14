package com.michaelfotiadis.heartbeat.ui.main.fragment.info.viewmodel

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import javax.inject.Inject

class DeviceInfoViewModel(private val bluetoothRepo: BluetoothRepo) : ViewModel() {

    val deviceInfoLiveData = Transformations.map(bluetoothRepo.deviceInfoLiveData)
    { deviceInfo ->
        deviceInfo
    }

}


class DeviceInfoViewModelFactory @Inject constructor(
    private val bluetoothRepo: BluetoothRepo
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeviceInfoViewModel(bluetoothRepo) as T
    }
}


