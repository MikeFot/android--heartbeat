package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceInfo
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo
import kotlinx.coroutines.delay
import java.util.*

private const val DELAY_MS = 2000L

class RefreshDeviceInfoInteractor(
    private val miServices: MiServices,
    private val bluetoothRepo: BluetoothRepo,
    private val messageRepo: MessageRepo
) {

    suspend fun execute(gatt: BluetoothGatt) {

        gatt.device?.let { device ->
            val deviceInfo = bluetoothRepo.deviceInfoLiveData.value ?: DeviceInfo()
            deviceInfo.name = device.name
            deviceInfo.address = device.address
            bluetoothRepo.deviceInfoLiveData.postValue(deviceInfo)
        }

        readCharacteristic(
            gatt,
            miServices.basicService.service,
            miServices.basicService.batteryCharacteristic
        )
        delay(DELAY_MS)
        readCharacteristic(
            gatt,
            miServices.deviceInformationService.service,
            miServices.deviceInformationService.serialNumberCharacteristic
        )
        delay(DELAY_MS)
        readCharacteristic(
            gatt,
            miServices.deviceInformationService.service,
            miServices.deviceInformationService.softwareRevisionCharacteristic
        )
        delay(DELAY_MS)
        readCharacteristic(
            gatt,
            miServices.deviceInformationService.service,
            miServices.deviceInformationService.hardwareRevisionCharacteristic
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