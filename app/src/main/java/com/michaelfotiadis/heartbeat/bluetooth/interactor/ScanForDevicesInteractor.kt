package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Completable

class ScanForDevicesInteractor(
    private val bleManager: BleManager,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(callback: (ScanStatus) -> Unit): Cancellable {

        val disposable = Completable.fromAction {
            bleManager.scan(ScanCallback(callback))
        }
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe()
        return DisposableCancellable(disposable)
    }

    inner class ScanCallback(private val callback: (ScanStatus) -> Unit) : BleScanCallback() {

        private val devices = mutableListOf<BleDevice>()

        override fun onScanFinished(scanResultList: List<BleDevice>?) {
            messageRepo.log("On Scan Finished with ${scanResultList?.size} items")
            callback.invoke(ScanStatus.Finished(scanResultList ?: listOf()))
        }

        override fun onScanStarted(success: Boolean) {
            messageRepo.log("On Scan Started '$success'")
            callback.invoke(ScanStatus.Started)
        }

        override fun onScanning(bleDevice: BleDevice?) {
            if (bleDevice != null) {
                messageRepo.log("On Scanning '${bleDevice.name} at address '${bleDevice.mac}'")
                devices.add(bleDevice)
                callback.invoke(ScanStatus.Scanning(devices))
            } else {
                messageRepo.log("On Scanning null device")
            }
        }
    }
}