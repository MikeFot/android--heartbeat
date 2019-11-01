package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.MutableLiveData
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionResult

class BluetoothStatusProvider {

    val isConnectedLiveData = MutableLiveData<Boolean>()
    val bondedDevicesLiveData = MutableLiveData<Set<BluetoothDevice>>()
    val connectionStatusLiveData = MutableLiveData<ConnectionResult>()

}