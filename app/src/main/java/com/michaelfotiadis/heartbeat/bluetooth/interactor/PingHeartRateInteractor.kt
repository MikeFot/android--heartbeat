package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Observable
import org.reactivestreams.Subscriber
import java.util.concurrent.TimeUnit

class PingHeartRateInteractor(
    private val bleManager: BleManager,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bleDevice: BleDevice): Cancellable {
        val disposable = Observable.interval(10, 10, TimeUnit.SECONDS)
            .flatMap { timestamp ->
                return@flatMap Observable.fromPublisher<Boolean> { publisher ->
                    pingHeartRate(bleDevice, timestamp, publisher)
                }
            }
            .subscribeOn(executionThreads.bleScheduler)
            .doOnSubscribe { messageRepo.log("Ping Subscribed") }
            .doOnDispose { messageRepo.log("Ping Disposed") }
            .subscribe()
        return DisposableCancellable(disposable)
    }

    private fun pingHeartRate(
        bleDevice: BleDevice,
        timestamp: Long,
        publisher: Subscriber<in Boolean>
    ) {
        with(miServices.heartRateService) {
            bleManager.write(
                bleDevice,
                this.service,
                this.controlPointCharacteristic,
                this.dataPing,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        messageRepo.log("Ping Successful at '$timestamp'")
                        publisher.onComplete()
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        messageRepo.log("Ping Failed : '${exception?.description}'")
                        publisher.onComplete()
                    }
                })
        }
    }

}