package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGattCharacteristic
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceInfo
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo

class UpdateCharacteristicInteractor(
    private val miServices: MiServices,
    private val bluetoothRepo: BluetoothRepo,
    private val messageRepo: MessageRepo
) {

    fun execute(characteristic: BluetoothGattCharacteristic) {

        val deviceInfo = bluetoothRepo.deviceInfoLiveData.value ?: DeviceInfo()
        when (characteristic.uuid) {
            miServices.genericAccessService.deviceNameCharacteristic -> {
                deviceInfo.name = characteristic.getStringValue(0)
                messageRepo.log("Updated device name '${deviceInfo.name}'")
            }
            miServices.basicService.batteryCharacteristic -> {
                deviceInfo.batteryLevel =
                    characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1)
                messageRepo.log("Updated battery info '${deviceInfo.batteryLevel}'")
            }
            miServices.deviceInformationService.serialNumberCharacteristic -> {
                deviceInfo.serialNumber = characteristic.getStringValue(0)
                messageRepo.log("Updated serial number '${deviceInfo.serialNumber}'")
            }
            miServices.deviceInformationService.hardwareRevisionCharacteristic -> {
                deviceInfo.hardwareRevision = characteristic.getStringValue(0)
                messageRepo.log("Updated Hardware Revision '${deviceInfo.hardwareRevision}'")
            }
            miServices.deviceInformationService.softwareRevisionCharacteristic -> {
                deviceInfo.softwareRevision = characteristic.getStringValue(0)
                messageRepo.log("Updated Software Revision '${deviceInfo.softwareRevision}'")
            }
        }
        bluetoothRepo.deviceInfoLiveData.postValue(deviceInfo)
    }
}