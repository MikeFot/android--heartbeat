package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model

import android.bluetooth.BluetoothDevice
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.model.BondState
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceType
import javax.inject.Inject

class UiBondedDeviceMapper @Inject constructor() {

    fun map(bluetoothDevices: Collection<BluetoothDevice>): List<UiBondedDevice> {
        val uiItems = ArrayList<UiBondedDevice>(bluetoothDevices.size)
        bluetoothDevices.forEach { device ->
            uiItems.add(
                UiBondedDevice(
                    name = device.name ?: "Unknown Device",
                    address = device.address,
                    bondedStatus = BondState.fromCode(device.bondState),
                    deviceType = DeviceType.fromCode(device.type)
                )
            )
        }
        return uiItems
    }

}
