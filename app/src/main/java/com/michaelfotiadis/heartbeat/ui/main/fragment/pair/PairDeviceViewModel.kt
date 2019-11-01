package com.michaelfotiadis.heartbeat.ui.main.fragment.pair

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionResult
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import javax.inject.Inject

class PairDeviceViewModel(
    bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val connectionResultLiveData = MutableLiveData<ConnectionResult>()

    fun connect(macAddress: String) {
        intentDispatcher.connectToMacAddress(macAddress)
    }

}

class PairDeviceViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PairDeviceViewModel(bluetoothStatusProvider, intentDispatcher) as T
    }
}