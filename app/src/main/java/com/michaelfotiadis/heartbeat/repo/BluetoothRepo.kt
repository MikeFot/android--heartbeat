package com.michaelfotiadis.heartbeat.repo

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chibatching.kotpref.KotprefModel
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.polidea.rxandroidble2.RxBleClient

class BluetoothRepo : KotprefModel() {

    val bluetoothStateLiveData = MutableLiveData<RxBleClient.State>()
    val bondedDevicesLiveData = MutableLiveData<Set<BluetoothDevice>>()
    val scanStatusLiveData = MutableLiveData<ScanStatus>()
    val connectionStatusLiveData = MutableLiveData<ConnectionStatus>()
    val heartRateStatus = MutableLiveData<HeartRateStatus>()
    val heartRateExists: LiveData<Boolean> =
        Transformations.map(heartRateStatus) { heartRateStatus ->
            when (heartRateStatus) {
                is HeartRateStatus.Success, is HeartRateStatus.Updated -> true
                is HeartRateStatus.Failed -> false
            }
        }

    var connectedMacAddress by nullableStringPref(default = null)

}
