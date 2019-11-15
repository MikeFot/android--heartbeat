package com.michaelfotiadis.heartbeat.bluetooth.model

class DeviceInfo {

    var name: String? = null
    var address: String? = null
    var batteryLevel: Int? = null
    var serialNumber: String? = null
    var softwareRevision: String? = null
    var hardwareRevision: String? = null

    override fun toString(): String {
        return "DeviceInfo(name=$name, batteryLevel=$batteryLevel, serialNumber=$serialNumber, softwareRevision=$softwareRevision, hardwareRevision=$hardwareRevision)"
    }

}