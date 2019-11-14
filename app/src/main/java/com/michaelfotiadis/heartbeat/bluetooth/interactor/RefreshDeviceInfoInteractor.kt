package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import kotlinx.coroutines.delay
import java.util.*

private const val DELAY_MS = 2000L

class RefreshDeviceInfoInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo
) {

    suspend fun execute(gatt: BluetoothGatt) {
        readCharacteristic(
            gatt,
            miServices.genericAccessService.service,
            miServices.genericAccessService.deviceNameCharacteristic
        )
        delay(DELAY_MS)
        readCharacteristic(
            gatt,
            miServices.basicService.service,
            miServices.basicService.batteryCharacteristic
        )
    }

    private fun readCharacteristic(
        gatt: BluetoothGatt,
        serviceUuid: UUID,
        characteristicUuid: UUID
    ) {
        try {
            val service = gatt.getService(serviceUuid)
            val characteristic = service.getCharacteristic(characteristicUuid)
            gatt.setCharacteristicNotification(characteristic, true)
            gatt.readCharacteristic(characteristic)
        } catch (e: Exception) {
            messageRepo.logError(e.message ?: "Device Info ERROR")
        }
    }
}