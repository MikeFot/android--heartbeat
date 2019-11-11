package com.michaelfotiadis.heartbeat.bluetooth.interactor.review

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.constants.UUIDs
import com.michaelfotiadis.heartbeat.bluetooth.interactor.DisposableCancellable
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.helpers.ValueInterpreter
import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Suppress("UNUSED_PARAMETER")
class AuthoriseInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {


    fun execute(
        rxBleDevice: RxBleDevice,
        rxBleConnection: RxBleConnection,
        callback: (ConnectionStatus) -> Unit
    ): DisposableCancellable {

        messageRepo.log("Asking for auth")


        val disposable =
            rxBleConnection.readCharacteristic(UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC)
                .doOnSuccess { bytes ->
                    messageRepo.log("Got Initial Auth Bytes $bytes")
                }
                .flatMap { bytes ->
                    val data = ValueInterpreter.getStringValue(bytes, 0)
                    val authData = ValueInterpreter.getStringValue(miServices.authService.authorisationBytes, 0)
                    messageRepo.log("Replacing value $data with $authData")
                    rxBleConnection.writeCharacteristic(
                        UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC,
                        miServices.authService.authorisationBytes
                    )
                }
                .flatMap { bytes ->
                    val data = ValueInterpreter.getStringValue(bytes, 0)
                    messageRepo.log("Got New Auth Value $data")
                    rxBleConnection.readCharacteristic(UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC)
                        .delaySubscription(200, TimeUnit.MILLISECONDS)
                }
                .flatMap { bytes ->
                    val data = ValueInterpreter.getStringValue(bytes, 0)
                    messageRepo.log("Executing Auth Sequence value = $data")
                    rxBleConnection.writeCharacteristic(
                        UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC,
                        getWriteBytes(bytes)
                    )
                        .delaySubscription(200, TimeUnit.MILLISECONDS)
                }
                .subscribe({ bytes ->
                    val data = ValueInterpreter.getStringValue(bytes, 0)
                    messageRepo.log("Final Bytes written $data")
                    callback.invoke(ConnectionStatus.Authorised(rxBleDevice, rxBleConnection))
                }, { throwable ->
                    messageRepo.logError(throwable?.message ?: "ERROR")
                    callback.invoke(ConnectionStatus.Failed(rxBleDevice, throwable))
                })

        return DisposableCancellable(disposable)
    }

    private fun getEnableDescriptorObservable(
        rxBleConnection: RxBleConnection
    ): Observable<Boolean> {
        return rxBleConnection.writeDescriptor(
            UUIDs.CUSTOM_SERVICE_FEE1,
            UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC,
            UUIDs.CUSTOM_SERVICE_AUTH_DESCRIPTOR,
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        )
            .toObservable<Boolean>()
    }

    /*------Methods to send requests to the device------*/
    private fun authoriseMiBand(bluetoothGatt: BluetoothGatt) {
        val service = bluetoothGatt.getService(UUIDs.CUSTOM_SERVICE_FEE1)
        val characteristic = service.getCharacteristic(UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC)
        bluetoothGatt.setCharacteristicNotification(characteristic, true)
        for (descriptor in characteristic.descriptors) {
            if (descriptor.uuid == UUIDs.CUSTOM_SERVICE_AUTH_DESCRIPTOR) {
                messageRepo.log(
                    "Found NOTIFICATION BluetoothGattDescriptor: " + descriptor.uuid.toString()
                )
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
        }

        characteristic.value = byteArrayOf(
            0x01,
            0x8,
            0x30,
            0x31,
            0x32,
            0x33,
            0x34,
            0x35,
            0x36,
            0x37,
            0x38,
            0x39,
            0x40,
            0x41,
            0x42,
            0x43,
            0x44,
            0x45
        )
        bluetoothGatt.writeCharacteristic(characteristic)
    }

    private fun getWriteBytes(value: ByteArray): ByteArray {
        return if (value[0].toInt() == 0x10 && value[1].toInt() == 0x01 && value[2].toInt() == 0x01) {
            messageRepo.log("Case 1")
            byteArrayOf(0x02, 0x8)
        } else if (value[0].toInt() == 0x10 && value[1].toInt() == 0x02 && value[2].toInt() == 0x01) {
            val tmpValue = Arrays.copyOfRange(value, 3, 19)
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")

            val key = SecretKeySpec(
                byteArrayOf(
                    0x30,
                    0x31,
                    0x32,
                    0x33,
                    0x34,
                    0x35,
                    0x36,
                    0x37,
                    0x38,
                    0x39,
                    0x40,
                    0x41,
                    0x42,
                    0x43,
                    0x44,
                    0x45
                ), "AES"
            )

            cipher.init(Cipher.ENCRYPT_MODE, key)
            val bytes = cipher.doFinal(tmpValue)

            val rq = byteArrayOf(0x03, 0x8).plus(bytes)
            messageRepo.log("Case 2")
            rq
        } else {
            messageRepo.log("Case 3")
            byteArrayOf()
        }
    }

    private fun executeAuthorisationSequence(bluetoothGatt: BluetoothGatt): Boolean {
        val service = bluetoothGatt.getService(UUIDs.CUSTOM_SERVICE_FEE1)
        val characteristic = service.getCharacteristic(UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC)
        val value = characteristic.value
        return if (value[0].toInt() == 0x10 && value[1].toInt() == 0x01 && value[2].toInt() == 0x01) {
            characteristic.value = byteArrayOf(0x02, 0x8)
            messageRepo.log("Case 1")
            bluetoothGatt.writeCharacteristic(characteristic)
        } else if (value[0].toInt() == 0x10 && value[1].toInt() == 0x02 && value[2].toInt() == 0x01) {
            val tmpValue = Arrays.copyOfRange(value, 3, 19)
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")

            val key = SecretKeySpec(
                byteArrayOf(
                    0x30,
                    0x31,
                    0x32,
                    0x33,
                    0x34,
                    0x35,
                    0x36,
                    0x37,
                    0x38,
                    0x39,
                    0x40,
                    0x41,
                    0x42,
                    0x43,
                    0x44,
                    0x45
                ), "AES"
            )

            cipher.init(Cipher.ENCRYPT_MODE, key)
            val bytes = cipher.doFinal(tmpValue)

            val rq = byteArrayOf(0x03, 0x8).plus(bytes)
            characteristic.value = rq
            messageRepo.log("Case 2")
            bluetoothGatt.writeCharacteristic(characteristic)
        } else {
            messageRepo.log("Case 3")
            false
        }
    }

}