package com.michaelfotiadis.heartbeat.bluetooth.interactor.review

import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.interactor.DisposableCancellable
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.NotificationSetupMode
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.helpers.ValueInterpreter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*
import java.util.concurrent.TimeUnit

class MeasureSingleHeartRateInteractor(
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(
        rxBleConnection: RxBleConnection,
        callback: (HeartRateStatus) -> Unit
    ): DisposableCancellable {

        val writeObservable = rxBleConnection.writeCharacteristic(
            UUID.fromString(miServices.heartRateService.controlPointCharacteristic.toString()),
            miServices.heartRateService.dataSingleMeasurement
        )
            .toObservable()
            .doOnNext { messageRepo.log("Bytes written") }
            .delaySubscription(200, TimeUnit.MILLISECONDS)
            .subscribeOn(executionThreads.bleScheduler)

        val notificationObservable = rxBleConnection.setupNotification(
            UUID.fromString(
                miServices.heartRateService.measurementCharacteristic.toString()
            ), NotificationSetupMode.DEFAULT
        )
            .doOnNext { messageRepo.log("Notification received") }
            .flatMap { notificationObservable -> notificationObservable }
            .doOnSubscribe { callback.invoke(HeartRateStatus.Success) }
            .subscribeOn(executionThreads.bleScheduler)

        val disposable = Observable.combineLatest(
            notificationObservable,
            writeObservable,
            BiFunction { notifyBytes: ByteArray, _: ByteArray ->
                ValueInterpreter.getIntValue(notifyBytes, ValueInterpreter.FORMAT_UINT8, 1)
            })
            .subscribeOn(executionThreads.bleScheduler)
            .singleOrError()
            .doOnSubscribe { messageRepo.log("Subscribed for heart rate") }
            .subscribe({ heartRate ->
                messageRepo.log("Heart Rate $heartRate")
                callback.invoke(HeartRateStatus.Updated(heartRate))
            }, { throwable ->
                callback.invoke(HeartRateStatus.Failed(throwable))
            })

        return DisposableCancellable(disposable)
    }
}