package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo

class NotifyAuthInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo
) {

    fun execute(gatt: BluetoothGatt) {
        val service = gatt.getService(miServices.authService.service)
        val characteristic = service.getCharacteristic(
            miServices.authService.authCharacteristic
        )
        gatt.setCharacteristicNotification(characteristic, true)
        characteristic.descriptors.forEach { descriptor ->
            if (descriptor.uuid == miServices.authService.authDescriptor) {
                messageRepo.log("Notifying Auth Descriptor ${descriptor.value}")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            } else {
                messageRepo.logError("Descriptor does not match")
            }
        }
        characteristic.value = miServices.authService.authorisationBytes
        messageRepo.log("Writing Auth Characteristic")
        gatt.writeCharacteristic(characteristic)
    }

}