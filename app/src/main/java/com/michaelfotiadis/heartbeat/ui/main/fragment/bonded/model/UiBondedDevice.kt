package com.michaelfotiadis.heartbeat.ui.main.fragment.bonded.model

import com.michaelfotiadis.heartbeat.bluetooth.BondState
import com.michaelfotiadis.heartbeat.bluetooth.DeviceType

data class UiBondedDevice(
    val name: String,
    val address: String,
    val bondedStatus: BondState,
    val deviceType: DeviceType
)