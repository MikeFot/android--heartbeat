package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ExecuteAuthSequenceInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo
) {

    enum class Result {
        STEP_1,
        STEP_2,
        DONE,
        ERROR
    }

    fun execute(gatt: BluetoothGatt): Result {
        val service = gatt.getService(miServices.authService.service)
        val characteristic = service.getCharacteristic(
            miServices.authService.authCharacteristic
        )
        messageRepo.log("Executing authorisation")
        val value: ByteArray? = characteristic.value
        return if (value != null && value.size >= 3) {
            if (value[0].toInt() == 0x10 && value[1].toInt() == 0x01 && value[2].toInt() == 0x01) {
                executeStepOne(characteristic, gatt)
            } else if (value[0].toInt() == 0x10 && value[1].toInt() == 0x02 && value[2].toInt() == 0x01) {
                executeStepTwo(value, characteristic, gatt)
            } else {
                messageRepo.log("Executing authorisation - Done")
                Result.DONE
            }
        } else {
            Result.ERROR
        }
    }

    private fun executeStepOne(
        characteristic: BluetoothGattCharacteristic,
        gatt: BluetoothGatt
    ): Result {
        characteristic.value = byteArrayOf(0x02, 0x8)
        messageRepo.log("Executing authorisation - Case 1")
        gatt.writeCharacteristic(characteristic)
        return Result.STEP_1
    }

    @SuppressLint("GetInstance")
    private fun executeStepTwo(
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic,
        gatt: BluetoothGatt
    ): Result {
        try {
            val tmpValue = value.copyOfRange(3, 19)
            val cipher = Cipher.getInstance(miServices.authService.cipherTransformation)

            val key = SecretKeySpec(
                miServices.authService.keyBytes, miServices.authService.aes
            )

            cipher.init(Cipher.ENCRYPT_MODE, key)
            val bytes = cipher.doFinal(tmpValue)


            val rq = byteArrayOf(0x03, 0x8).plus(bytes)
            characteristic.value = rq
            messageRepo.log("Executing authorisation - Case 2")
            gatt.writeCharacteristic(characteristic)
            return Result.STEP_2
        } catch (e: Exception) {
            messageRepo.logError(e.message ?: "ERROR")
            return Result.ERROR
        }
    }
}