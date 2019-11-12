package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceResult
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import io.reactivex.Single

class GetBondedDevicesInteractor(
    private val bleManager: BleManager,
    private val executionThreads: ExecutionThreads
) {

    fun execute(callback: (List<DeviceResult>) -> Unit): Cancellable {

        val disposable = Single.fromCallable {
            bleManager.bluetoothAdapter.bondedDevices
        }.map { bondedDevices ->
            ArrayList<DeviceResult>().apply {
                bondedDevices.forEach { bondedDevice ->
                    add(DeviceResult.fromBluetoothDevice(bondedDevice))
                }
            }
        }
            .onErrorReturn { ArrayList() }
            .doOnSuccess(callback::invoke)
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe()
        return DisposableCancellable(disposable)
    }
}