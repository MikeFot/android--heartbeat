package com.michaelfotiadis.heartbeat.bluetooth.model

import com.clj.fastble.data.BleDevice

sealed class ScanStatus {

    object Started : ScanStatus()

    data class Scanning(val bleDevices: List<BleDevice>) : ScanStatus()

    data class Finished(val bleDevices: List<BleDevice>) : ScanStatus()
}
