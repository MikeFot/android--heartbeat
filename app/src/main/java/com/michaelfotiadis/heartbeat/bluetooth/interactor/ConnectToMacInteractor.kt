package com.michaelfotiadis.heartbeat.bluetooth.interactor

import android.bluetooth.BluetoothGatt
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.exception.OtherException
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import io.reactivex.Observable
import org.reactivestreams.Subscriber

class ConnectToMacInteractor(
    private val bleManager: BleManager,
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
            .subscribe { connectionStatus -> callback.invoke(connectionStatus) }
        return DisposableCancellable(disposable)
    }

    class ConnectionCallback(private val publisher: Subscriber<in ConnectionStatus>) :
        BleGattCallback() {
        override fun onStartConnect() {
            publisher.onNext(ConnectionStatus.Started)
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
            publisher.onNext(
                ConnectionStatus.Disconnected(isActiveDisConnected, device, gatt, status)
            )
        }

        override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            publisher.onNext(ConnectionStatus.Connected(bleDevice, gatt, status))
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            publisher.onNext(ConnectionStatus.Failed(bleDevice, exception))
        }
    }
}