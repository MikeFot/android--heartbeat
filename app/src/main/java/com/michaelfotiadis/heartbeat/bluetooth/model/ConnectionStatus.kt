package com.michaelfotiadis.heartbeat.bluetooth.model

import android.bluetooth.BluetoothGatt
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException

sealed class ConnectionStatus {

    object Started : ConnectionStatus()

    data class Connected(
        val bleDevice: BleDevice?,
        val gatt: BluetoothGatt?,
        val status: Int
    ) : ConnectionStatus()

    data class Disconnected(
        val isActiveDisConnected: Boolean,
        val device: BleDevice?,
        val gatt: BluetoothGatt?,
        val status: Int
    ) : ConnectionStatus()

    data class Failed(
        val bleDevice: BleDevice?,
        val exception: BleException?
    ) : ConnectionStatus()
}
