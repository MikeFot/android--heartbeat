package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ExecuteAuthorisationSequenceInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo
) {

    enum class Result {
        STEP_1,
        STEP_2,
        DONE,
        ERROR
    }

    @SuppressLint("GetInstance")
    fun execute(gatt: BluetoothGatt): Result {
        var result: Result
        val service = gatt.getService(miServices.authService.service)
        val characteristic = service.getCharacteristic(
            miServices.authService.authCharacteristic
        )
        messageRepo.log("Executing authorisation")
        val value = characteristic.value
        if (value[0].toInt() == 0x10 && value[1].toInt() == 0x01 && value[2].toInt() == 0x01) {
            characteristic.value = byteArrayOf(0x02, 0x8)
            messageRepo.log("Executing authorisation - Case 1")
            gatt.writeCharacteristic(characteristic)
            result = Result.STEP_1
        } else if (value[0].toInt() == 0x10 && value[1].toInt() == 0x02 && value[2].toInt() == 0x01) {
            try {
                val tmpValue = Arrays.copyOfRange(value, 3, 19)
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
                result = Result.STEP_2
            } catch (e: Exception) {
                messageRepo.logError(e.message ?: "ERROR")
                result = Result.ERROR
            }
        } else {
            messageRepo.log("Executing authorisation - Done")
            result = Result.DONE
        }
        return result
    }
}