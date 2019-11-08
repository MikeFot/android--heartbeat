package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleWriteCallback
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

class MeasureSingleHeartRateInteractor(
    private val bleManager: BleManager,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bleDevice: BleDevice, callback: (HeartRateStatus) -> Unit): DisposableCancellable {

        val disposable = writeAskForSingleMeasurement(bleDevice)
            .delay(100, TimeUnit.MILLISECONDS)
            .subscribeOn(executionThreads.bleScheduler)
            .doOnNext { success ->
                if (success) {
                    notifyHeartRateMeasurement(bleDevice)
                        .subscribeOn(executionThreads.bleScheduler)
                        .doOnNext(callback::invoke)
                        .subscribe()
                } else {
                    callback.invoke(HeartRateStatus.Failed("Failed to start Heart Rate monitoring"))
                }
            }
            .subscribe()
        return DisposableCancellable(disposable)
    }

    private fun writeAskForSingleMeasurement(bleDevice: BleDevice): Observable<Boolean> {

        return Observable.fromPublisher<Boolean> { publisher ->
            bleManager.write(
                bleDevice,
                miServices.heartRateService.service,
                miServices.heartRateService.controlPointCharacteristic,
                miServices.heartRateService.dataSingleMeasurement,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        messageRepo.log("Single Heart Rate Write Successful")
                        publisher.onNext(true)
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        messageRepo.log("Single Heart Rate Write Failed: ${exception?.description}")
                        publisher.onNext(false)
                        publisher.onComplete()
                    }
                }
            )
        }

    }

    private fun notifyHeartRateMeasurement(bleDevice: BleDevice): Observable<HeartRateStatus> {
        return Observable.fromPublisher { publisher ->
            bleManager.notify(
                bleDevice,
                miServices.heartRateService.service,
                miServices.heartRateService.measurementCharacteristic,
                NotifyCallback(publisher, messageRepo)
            )
        }
    }

    private class NotifyCallback(
        private val publisher: Subscriber<in HeartRateStatus>,
        private val messageRepo: MessageRepo
    ) :
        BleNotifyCallback() {
        override fun onCharacteristicChanged(data: ByteArray?) {
            val message = Arrays.toString(data) ?: ""
            messageRepo.log("DATA: $message")
            if (data != null && data.size >= 2) {
                val heartRate = data[1]
                publisher.onNext(HeartRateStatus.Updated(heartRate.toInt()))
                publisher.onComplete()
            }
        }

        override fun onNotifyFailure(exception: BleException?) {
            messageRepo.log("EX: ${exception?.description}")
            publisher.onNext(
                HeartRateStatus.Failed(
                    exception?.description ?: "Failed to notify heart rate"
                )
            )
            publisher.onComplete()
        }

        override fun onNotifySuccess() {
            messageRepo.log("Notify success")
            publisher.onNext(HeartRateStatus.Success)
        }
    }

}