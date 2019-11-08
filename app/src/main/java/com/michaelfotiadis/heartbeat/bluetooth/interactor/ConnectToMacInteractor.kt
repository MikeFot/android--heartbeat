package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.exception.OtherException
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Observable
import org.reactivestreams.Subscriber

class ConnectToMacInteractor(
    private val bleManager: BleManager,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(
        macAddress: String,
        callback: (ConnectionStatus) -> Unit
    ): DisposableCancellable {

        val disposable = Observable.fromPublisher<ConnectionStatus> { publisher ->
            bleManager.connect(macAddress, ConnectionCallback(publisher))
        }
            .onErrorReturn { throwable ->
                ConnectionStatus.Failed(
                    null,
                    OtherException(throwable.message)
                )
            }
            .subscribeOn(executionThreads.bleScheduler)
            .doOnSubscribe { messageRepo.log("Attempting to connect to $macAddress") }
            .doOnComplete { messageRepo.log("Connection completed") }
            .doOnNext(callback::invoke)
            .subscribe()
        return DisposableCancellable(disposable)
    }

    inner class ConnectionCallback(private val publisher: Subscriber<in ConnectionStatus>) :
        BleGattCallback() {
        override fun onStartConnect() {
            messageRepo.log("Connection Started")
            publisher.onNext(ConnectionStatus.Started)
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            messageRepo.log("Disconnected from device ${device?.mac}")
            publisher.onNext(
                ConnectionStatus.Disconnected(isActiveDisConnected, device, gatt, status)
            )
        }

        override fun onConnectSuccess(device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            messageRepo.log("Connected to device ${device?.mac}")
            publisher.onNext(ConnectionStatus.Connected(device, gatt, status))
        }

        override fun onConnectFail(device: BleDevice?, exception: BleException?) {
            messageRepo.log("Connection failed with device ${device?.mac} with error ${exception?.description}")
            publisher.onNext(ConnectionStatus.Failed(device, exception))
        }
    }
}