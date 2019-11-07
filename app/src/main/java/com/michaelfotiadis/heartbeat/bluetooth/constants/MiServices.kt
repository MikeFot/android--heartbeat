package com.michaelfotiadis.heartbeat.bluetooth.constants

import javax.inject.Inject

class MiServices @Inject constructor() {

    val heartRateService = HeartRateService()

    data class HeartRateService(
        val service: String = "0000180d-0000-1000-8000-00805f9b34fb",
        val measurementCharacteristic: String = "00002a37-0000-1000-8000-00805f9b34fb",
        val measurementDescriptor: String = "00002902-0000-1000-8000-00805f9b34fb",
        val controlPointCharacteristic: String = "00002a39-0000-1000-8000-00805f9b34fb"
    )

}