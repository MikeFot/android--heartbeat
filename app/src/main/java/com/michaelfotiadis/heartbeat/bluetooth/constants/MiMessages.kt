package com.michaelfotiadis.heartbeat.bluetooth.constants

object MiMessages {
    val HMC_SINGLE_MEASUREMENT = byteArrayOf(0x15, 0x02, 0x01)
    val HMC_CONTINUOUS_MEASUREMENT = byteArrayOf(0x15, 0x01, 0x01)
    val HMC_PING = byteArrayOf(0x16)
}