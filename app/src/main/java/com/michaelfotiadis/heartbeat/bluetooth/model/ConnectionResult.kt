package com.michaelfotiadis.heartbeat.bluetooth.model

import android.bluetooth.BluetoothGatt
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException

sealed class ConnectionResult {

    object Started : ConnectionResult()

    data class Connected(
        val bleDevice: BleDevice?,
        val gatt: BluetoothGatt?,
        val status: Int
    ) : ConnectionResult()

    data class Disconnected(
        val isActiveDisConnected: Boolean,
        val device: BleDevice?,
        val gatt: BluetoothGatt?,
        val status: Int
    ) : ConnectionResult()

    data class Failed(
        val bleDevice: BleDevice?, val exception: BleException?
    ) : ConnectionResult()

}