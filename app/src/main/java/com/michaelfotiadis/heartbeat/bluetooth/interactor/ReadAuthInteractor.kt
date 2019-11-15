package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices

class ReadAuthInteractor(
    private val miServices: MiServices
) {

    fun execute(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(miServices.authService.service)
        return if (service != null) {
            val characteristic = service.getCharacteristic(
                miServices.authService.authCharacteristic
            )
            if (characteristic != null) {
                gatt.readCharacteristic(characteristic)
                true
            } else {
                false
            }
        } else {
            false
        }
    }

}