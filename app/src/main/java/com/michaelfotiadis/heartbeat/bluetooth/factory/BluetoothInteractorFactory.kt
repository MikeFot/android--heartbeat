package com.michaelfotiadis.heartbeat.bluetooth.factory

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.interactor.AuthoriseMiBandInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ExecuteAuthorisationSequenceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.AuthoriseInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.CancelScanInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.CleanupBluetoothInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.ConnectToMacInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.DisconnectDeviceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.GetBondedDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.IsBluetoothOnInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.MeasureSingleHeartRateInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.ObserveBluetoothInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.ObserveConnectionInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.PingHeartRateInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.ScanForDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.StartNotifyHeartServiceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.StopHeartRateMeasurementInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.review.StopNotifyHeartServiceInteractor
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import com.polidea.rxandroidble2.RxBleClient

class BluetoothInteractorFactory(
    private val rxBleClient: RxBleClient,
    private val bleManager: BleManager,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    val authoriseMiBandInteractor: AuthoriseMiBandInteractor by lazy {
        AuthoriseMiBandInteractor(miServices, messageRepo)
    }

    val executeAuthorisationSequenceInteractor: ExecuteAuthorisationSequenceInteractor by lazy {
        ExecuteAuthorisationSequenceInteractor(miServices, messageRepo)
    }

    val pingHeartRateInteractor: PingHeartRateInteractor by lazy {
        PingHeartRateInteractor(
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val authoriseInteractor: AuthoriseInteractor by lazy {
        AuthoriseInteractor(
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val observeBluetoothInteractor: ObserveBluetoothInteractor by lazy {
        ObserveBluetoothInteractor(
            rxBleClient,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val isBluetoothOnInteractor: IsBluetoothOnInteractor by lazy {
        IsBluetoothOnInteractor(
            bleManager,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val cleanupBluetoothOnInteractor: CleanupBluetoothInteractor by lazy {
        CleanupBluetoothInteractor(
            bleManager,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val disconnectDeviceInteractor: DisconnectDeviceInteractor by lazy {
        DisconnectDeviceInteractor(
            bleManager,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val cancelScanInteractor: CancelScanInteractor by lazy {
        CancelScanInteractor(
            bleManager,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val stopNotifyHeartServiceInteractor: StopNotifyHeartServiceInteractor by lazy {
        StopNotifyHeartServiceInteractor(
            bleManager,
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val startNotifyHeartServiceInteractor: StartNotifyHeartServiceInteractor by lazy {
        StartNotifyHeartServiceInteractor(
            bleManager,
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val scanDevicesInteractor: ScanForDevicesInteractor by lazy {
        ScanForDevicesInteractor(
            bleManager,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val connectToMacInteractor: ConnectToMacInteractor by lazy {
        ConnectToMacInteractor(
            rxBleClient,
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val observeConnectionInteractor: ObserveConnectionInteractor by lazy {
        ObserveConnectionInteractor(
            rxBleClient,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val measureSingleHeartRateInteractor: MeasureSingleHeartRateInteractor by lazy {
        MeasureSingleHeartRateInteractor(
            miServices,
            messageRepo,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val getBondedDevicesInteractor: GetBondedDevicesInteractor by lazy {
        GetBondedDevicesInteractor(
            bleManager,
            executionThreads
        )
    }

    @Deprecated("Using Old BLE")
    val stopHeartRateMeasurementInteractor: StopHeartRateMeasurementInteractor by lazy {
        StopHeartRateMeasurementInteractor(
            bleManager,
            miServices,
            messageRepo,
            executionThreads
        )
    }

}