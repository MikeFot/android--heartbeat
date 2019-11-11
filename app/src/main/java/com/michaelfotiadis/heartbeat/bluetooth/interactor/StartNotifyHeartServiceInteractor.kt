package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Observable
import org.reactivestreams.Subscriber
import java.util.*
import java.util.concurrent.TimeUnit

class StartNotifyHeartServiceInteractor(
    private val bleManager: BleManager,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bleDevice: BleDevice, callback: (HeartRateStatus) -> Unit): Cancellable {

        val disposable = Observable.fromPublisher<HeartRateStatus> { publisher ->
            bleManager.notify(
                bleDevice,
                miServices.heartRateService.service,
                miServices.heartRateService.measurementCharacteristic,
                NotifyCallback(publisher, messageRepo)
            )
        }
            .delaySubscription(1, TimeUnit.SECONDS)
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(executionThreads.bleScheduler)
            .doOnNext(callback::invoke)
            .subscribe()
        return DisposableCancellable(disposable)
    }

    private class NotifyCallback(
        private val publisher: Subscriber<in HeartRateStatus>,
        private val messageRepo: MessageRepo
    ) :
        BleNotifyCallback() {
        override fun onCharacteristicChanged(data: ByteArray?) {
            val message = Arrays.toString(data) ?: ""
            messageRepo.log("Notify DATA: $message")
            if (data != null && data.size >= 2) {
                val heartRate = data[1]
                publisher.onNext(HeartRateStatus.Updated(heartRate.toInt()))
            }
        }

        override fun onNotifyFailure(exception: BleException?) {
            messageRepo.logError("Notify EX: ${exception?.description}")
            /*publisher.onNext(
                HeartRateStatus.Failed(exception)
            )*/
        }

        override fun onNotifySuccess() {
            messageRepo.log("Notify success")
            publisher.onNext(HeartRateStatus.Success)
        }
    }
}