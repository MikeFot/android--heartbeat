package com.michaelfotiadis.heartbeat.bluetooth.constants

import javax.inject.Inject

class MiServices @Inject constructor() {

    val heartRateService = HeartRateService()

    class HeartRateService {
        val service: String = "0000180d-0000-1000-8000-00805f9b34fb"
        val measurementCharacteristic: String = "00002a37-0000-1000-8000-00805f9b34fb"
        val measurementDescriptor: String = "00002902-0000-1000-8000-00805f9b34fb"
        val controlPointCharacteristic: String = "00002a39-0000-1000-8000-00805f9b34fb"
        val dataPing: ByteArray = byteArrayOf(0x16)
        val dataSingleMeasurement = byteArrayOf(0x15, 0x02, 0x01)
        val dataContinuousMeasurement = byteArrayOf(0x15, 0x01, 0x01)
    }

    val authorisationBytes = byteArrayOf(
        0x01,
        0x8,
        0x30,
        0x31,
        0x32,
        0x33,
        0x34,
        0x35,
        0x36,
        0x37,
        0x38,
        0x39,
        0x40,
        0x41,
        0x42,
        0x43,
        0x44,
        0x45
    )
}