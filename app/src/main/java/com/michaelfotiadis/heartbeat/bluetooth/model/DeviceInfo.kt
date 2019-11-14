package com.michaelfotiadis.heartbeat.bluetooth.model

class DeviceInfo {

    var name: String? = null
    var batteryLevel: Int? = null

    override fun toString(): String {
        return "DeviceInfo(name=$name, batteryLevel=$batteryLevel)"
    }
}