package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceResult
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo
import io.reactivex.Observable
import org.reactivestreams.Subscriber

class ScanForDevicesInteractor(
    private val bleManager: BleManager,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(callback: (ScanStatus) -> Unit): Cancellable {

        val disposable = Observable.fromPublisher<ScanStatus> { publisher ->
            bleManager.scan(ScanCallback(publisher))
        }
            .subscribeOn(executionThreads.bleScheduler)
            .doOnNext(callback::invoke)
            .doOnDispose { messageRepo.log("Scan disposed") }
            .subscribe()
        return DisposableCancellable(disposable)
    }

    inner class ScanCallback(private val publisher: Subscriber<in ScanStatus>) : BleScanCallback() {

        private val devices = mutableListOf<DeviceResult>()

        override fun onScanFinished(scanResultList: List<BleDevice>?) {
            messageRepo.log("On Scan Finished with ${scanResultList?.size} items")
            devices.clear()
            devices.addAll(DeviceResult.fromBleDevices(scanResultList ?: listOf()))
            publisher.onNext(ScanStatus.Finished(devices))
            publisher.onComplete()
        }

        override fun onScanStarted(success: Boolean) {
            messageRepo.log("On Scan Started '$success'")
            publisher.onNext(ScanStatus.Started)
        }

        override fun onScanning(bleDevice: BleDevice?) {
            if (bleDevice != null) {
                messageRepo.log("On Scanning '${bleDevice.name} at address '${bleDevice.mac}'")
                devices.add(DeviceResult.fromBleDevice(bleDevice))
                publisher.onNext(ScanStatus.Scanning(devices))
            } else {
                messageRepo.log("On Scanning null device")
            }
        }
    }
}