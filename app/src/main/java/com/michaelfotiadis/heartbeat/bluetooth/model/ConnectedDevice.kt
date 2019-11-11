package com.michaelfotiadis.heartbeat.bluetooth.model

import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice

data class ConnectedDevice(
    val mac: String,
    val name: String? = null,
    val rxBleDevice: RxBleDevice,
    val rxBleConnection: RxBleConnection
) {
    val displayIdentifier: String = name ?: mac
}