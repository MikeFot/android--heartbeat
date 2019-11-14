package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices

class ReadAuthInteractor(
    private val miServices: MiServices
) {

    fun execute(gatt: BluetoothGatt) {
        val service = gatt.getService(miServices.authService.service)
        val characteristic = service.getCharacteristic(
            miServices.authService.authCharacteristic
        )
        gatt.readCharacteristic(characteristic)
    }

}