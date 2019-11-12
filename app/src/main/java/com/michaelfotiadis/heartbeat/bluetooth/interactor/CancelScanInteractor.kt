package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.data.BleScanState
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import io.reactivex.Completable

class CancelScanInteractor(
    private val bleManager: BleManager,
    private val executionThreads: ExecutionThreads
) {

    fun execute(): Cancellable {
        val disposable = Completable.fromAction {
            if (bleManager.scanSate == BleScanState.STATE_SCANNING) {
                bleManager.cancelScan()
            }
        }
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe()
        return DisposableCancellable(disposable)
    }
}