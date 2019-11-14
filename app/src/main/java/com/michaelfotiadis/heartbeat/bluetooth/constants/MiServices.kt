package com.michaelfotiadis.heartbeat.bluetooth.constants

import java.util.*
import javax.inject.Inject

class MiServices @Inject constructor() {

    val genericAccessService = GenericAccessService()
    val authService = AuthService()
    val basicService = BasicService()
    val heartRateService = HeartRateService()

    class GenericAccessService {
        val service: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
        val deviceNameCharacteristic: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    }

    class AuthService {
        val service: UUID = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")
        val authCharacteristic: UUID = UUID.fromString("00000009-0000-3512-2118-0009af100700")
        val authDescriptor: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val authorisationBytes = byteArrayOf(
            0x01, 0x8, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35,
            0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45
        )
        val keyBytes = byteArrayOf(
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
            0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45
        )
        val cipherTransformation = "AES/ECB/NoPadding"
        val aes = "AES"
    }

    class BasicService {
        var service: UUID = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb")
        var batteryCharacteristic: UUID = UUID.fromString("00000006-0000-3512-2118-0009af100700")
    }

    class HeartRateService {
        val service: UUID = UUID
            .fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val measurementCharacteristic: UUID = UUID
            .fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val measurementDescriptor: UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb")
        val controlPointCharacteristic: UUID = UUID
            .fromString("00002a39-0000-1000-8000-00805f9b34fb")
        val dataPing: ByteArray = byteArrayOf(0x16)
        val dataSingleMeasurement = byteArrayOf(0x15, 0x02, 0x01)
        val dataContinuousMeasurement = byteArrayOf(0x15, 0x01, 0x01)
    }

    fun isAuthorised(data: ByteArray?): Boolean {
        return when {
            data == null || data.size < 3 -> false
            data[0].toInt() == 0x10 && data[1].toInt() == 0x01 && data[2].toInt() == 0x01 -> false
            data[0].toInt() == 0x10 && data[1].toInt() == 0x02 && data[2].toInt() == 0x01 -> false
            else -> true
        }
    }
}