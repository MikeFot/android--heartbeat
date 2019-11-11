package com.michaelfotiadis.heartbeat.bluetooth.interactor.review

import com.clj.fastble.BleManager
import com.clj.fastble.data.BleScanState
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.interactor.DisposableCancellable
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Completable

class CancelScanInteractor(
    private val bleManager: BleManager,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(): Cancellable {
        val disposable = Completable.fromAction {
            if (bleManager.scanSate == BleScanState.STATE_SCANNING) {
                bleManager.cancelScan()
            }
        }
            .doOnComplete { messageRepo.log("Cancelled Scan") }
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe()
        return DisposableCancellable(disposable)
    }
}