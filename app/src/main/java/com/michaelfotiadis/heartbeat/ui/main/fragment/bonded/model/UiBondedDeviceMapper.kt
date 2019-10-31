package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model

import android.bluetooth.BluetoothDevice
import com.michaelfotiadis.heartbeat.bluetooth.DeviceType
import com.michaelfotiadis.heartbeat.bluetooth.BondState
import javax.inject.Inject

class UiBondedDeviceMapper @Inject constructor() {

    fun map(bluetoothDevices: Collection<BluetoothDevice>): List<UiBondedDevice> {
        val uiItems = ArrayList<UiBondedDevice>(bluetoothDevices.size)
        bluetoothDevices.forEach { device ->
            uiItems.add(
                UiBondedDevice(
                    name = device.name,
                    address = device.address,
                    bondedStatus = BondState.fromCode(device.bondState),
                    deviceType = DeviceType.fromCode(device.type)
                )
            )
        }
        return uiItems
    }

}