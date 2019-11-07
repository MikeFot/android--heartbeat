package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import io.reactivex.Completable

class DisconnectDeviceInteractor(
    private val bleManager: BleManager,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bleDevice: BleDevice): Cancellable {
        val disposable = Completable.fromAction {
            bleManager.disconnect(bleDevice)
        }
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe()
        return DisposableCancellable(disposable)
    }
}