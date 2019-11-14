package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class PingHeartRateInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bluetoothGatt: BluetoothGatt): Cancellable {
        val disposable = Observable.interval(10, 8, TimeUnit.SECONDS)
            .flatMap { count ->
                return@flatMap Observable.fromPublisher<Boolean> { publisher ->
                    val service = bluetoothGatt.getService(miServices.heartRateService.service)
                    val characteristic = service.getCharacteristic(
                        miServices.heartRateService.controlPointCharacteristic
                    )
                    characteristic.value = miServices.heartRateService.dataPing
                    bluetoothGatt.writeCharacteristic(characteristic)
                    messageRepo.log("Ping Happened count= $count")
                    publisher.onComplete()
                }
            }
            .subscribeOn(executionThreads.bleScheduler)
            .doOnSubscribe { messageRepo.log("Ping Subscribed") }
            .doOnDispose { messageRepo.log("Ping Disposed") }
            .subscribe()
        return DisposableCancellable(disposable)
    }
}