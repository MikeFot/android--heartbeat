package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model

import com.michaelfotiadis.heartbeat.bluetooth.model.BondState
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceResult
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceType
import javax.inject.Inject

class UiBondedDeviceMapper @Inject constructor() {

    fun map(deviceResults: Set<DeviceResult>): List<UiBondedDevice> {
        return ArrayList<UiBondedDevice>(deviceResults.size).apply {
            deviceResults.forEach { device ->
                add(
                    UiBondedDevice(
                        name = device.name,
                        address = device.address,
                        bondedStatus = BondState.fromCode(device.bondedStatus),
                        deviceType = DeviceType.fromCode(device.deviceType),
                        rssi = device.rssi
                    )
                )
            }
        }
    }

}
