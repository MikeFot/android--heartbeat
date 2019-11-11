package com.michaelfotiadis.heartbeat.bluetooth.interactor.review

import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.interactor.DisposableCancellable
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Completable

class StopNotifyHeartServiceInteractor(
    private val bleManager: BleManager,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bleDevice: BleDevice): Cancellable {
        val disposable = Completable.fromAction {
            bleManager.stopNotify(
                bleDevice,
                miServices.heartRateService.service.toString(),
                miServices.heartRateService.measurementCharacteristic.toString()
            )
        }
            .doOnSubscribe { messageRepo.log("Stop Heart Rate Notify on ${bleDevice.mac}") }
            .subscribeOn(executionThreads.bleScheduler)
            .doOnComplete { messageRepo.log("Wrote Stop Notify Heart Service") }
            .subscribe()
        return DisposableCancellable(disposable)
    }
}