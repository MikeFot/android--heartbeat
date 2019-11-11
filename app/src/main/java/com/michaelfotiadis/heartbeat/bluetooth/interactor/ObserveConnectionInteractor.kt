package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection

class ObserveConnectionInteractor(
    private val rxBleClient: RxBleClient,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(
        macAddress: String,
        callback: (RxBleConnection.RxBleConnectionState) -> Unit
    ): DisposableCancellable {

        val device = rxBleClient.getBleDevice(macAddress)

        val disposable = device.observeConnectionStateChanges()
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe { state ->
                messageRepo.log("Connection State '$state'")
                callback.invoke(state)
            }
        return DisposableCancellable(disposable)
    }
}