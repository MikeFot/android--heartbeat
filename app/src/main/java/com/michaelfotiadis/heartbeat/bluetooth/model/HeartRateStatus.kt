package com.michaelfotiadis.heartbeat.bluetooth.model

sealed class HeartRateStatus {

    object Success : HeartRateStatus()

    data class Updated(val heartRate: Int) : HeartRateStatus()

    data class Failed(val error: String) : HeartRateStatus()
}
