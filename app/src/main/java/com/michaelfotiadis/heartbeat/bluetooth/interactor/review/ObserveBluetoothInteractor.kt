package com.michaelfotiadis.heartbeat.bluetooth.interactor.review

import com.michaelfotiadis.heartbeat.bluetooth.interactor.DisposableCancellable
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.RxBleClient

class ObserveBluetoothInteractor(
    private val rxBleClient: RxBleClient,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(callback: (RxBleClient.State) -> Unit): DisposableCancellable {

        val disposable = rxBleClient.observeStateChanges()
            .startWith(rxBleClient.state)
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe { state ->
                messageRepo.log("Bluetooth State $state")
                callback.invoke(state)
            }
        return DisposableCancellable(disposable)
    }
}