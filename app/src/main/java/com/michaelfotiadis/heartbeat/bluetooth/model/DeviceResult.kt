package com.michaelfotiadis.heartbeat.bluetooth.model

import android.bluetooth.BluetoothDevice
import com.clj.fastble.data.BleDevice

data class DeviceResult(
    val address: String,
    val name: String,
    val bondedStatus: Int,
    val deviceType: Int,
    val rssi: Int
) {
    companion object {

        fun fromBluetoothDevice(bluetoothDevice: BluetoothDevice): DeviceResult {
            return DeviceResult(
                bluetoothDevice.address,
                bluetoothDevice.name ?: "UNKNOWN DEVICE",
                bluetoothDevice.bondState,
                bluetoothDevice.type,
                0
            )
        }

        fun fromBleDevice(bleDevice: BleDevice): DeviceResult {
            return DeviceResult(
                bleDevice.mac,
                bleDevice.name ?: "UNKNOWN DEVICE",
                bleDevice.device.bondState,
                bleDevice.device.type,
                bleDevice.rssi
            )
        }

        fun fromBleDevices(bleDevices: Collection<BleDevice>): List<DeviceResult> {
            return mutableListOf<DeviceResult>().apply {
                bleDevices.forEach { bleDevice -> add(fromBleDevice(bleDevice)) }
            }
        }

    }
}