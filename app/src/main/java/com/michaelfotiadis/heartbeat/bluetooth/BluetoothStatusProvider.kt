package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus

class BluetoothStatusProvider {

    val isConnectedLiveData = MutableLiveData<Boolean>()
    val bondedDevicesLiveData = MutableLiveData<Set<BluetoothDevice>>()
    val scanStatusLiveData = MutableLiveData<ScanStatus>()
    val connectionStatusLiveData = MutableLiveData<ConnectionStatus>()
    val connectedDevice = Transformations.map(connectionStatusLiveData) { connectionStatus ->
        if (connectionStatus is ConnectionStatus.Connected) {
            connectionStatus.bleDevice
        } else {
            null
        }
    }
    val heartRateStatus = MutableLiveData<HeartRateStatus>()
}
