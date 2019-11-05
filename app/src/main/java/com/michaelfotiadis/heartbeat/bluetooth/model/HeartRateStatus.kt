package com.michaelfotiadis.heartbeat.bluetooth.model

import com.clj.fastble.exception.BleException

sealed class HeartRateStatus {

    object Success : HeartRateStatus()

    data class Updated(val heartRate: Int) : HeartRateStatus()

    data class Failed(val exception: BleException?) : HeartRateStatus()
}
