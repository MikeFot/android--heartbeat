package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class StopHeartRateMeasurementInteractor(
    private val bleManager: BleManager,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    fun execute(bleDevice: BleDevice, callback: () -> Unit): DisposableCancellable {

        val disposable = writeAskForSingleMeasurement(bleDevice)
            .delay(100, TimeUnit.MILLISECONDS)
            .subscribeOn(executionThreads.bleScheduler)
            .subscribe { callback.invoke() }
        return DisposableCancellable(disposable)
    }

    private fun writeAskForSingleMeasurement(bleDevice: BleDevice): Completable {

        return Completable.fromPublisher<Boolean> { publisher ->
            bleManager.write(
                bleDevice,
                miServices.heartRateService.service,
                miServices.heartRateService.controlPointCharacteristic,
                miServices.heartRateService.dataSingleMeasurement,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        messageRepo.log("Single Heart Rate Write Successful")
                        publisher.onComplete()
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        messageRepo.log("Single Heart Rate Write Failed: ${exception?.description}")
                        publisher.onComplete()
                    }
                }
            )
        }

    }
}