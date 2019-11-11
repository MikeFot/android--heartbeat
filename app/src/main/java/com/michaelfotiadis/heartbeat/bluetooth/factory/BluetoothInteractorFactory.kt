package com.michaelfotiadis.heartbeat.bluetooth.factory

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.interactor.AuthoriseInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.CancelScanInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.CleanupBluetoothInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ConnectToMacInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.DisconnectDeviceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.GetBondedDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.IsBluetoothOnInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.MeasureSingleHeartRateInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ObserveBluetoothInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ObserveConnectionInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.PingHeartRateInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ScanForDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.StartNotifyHeartServiceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.StopHeartRateMeasurementInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.StopNotifyHeartServiceInteractor
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

    val authoriseInteractor: AuthoriseInteractor by lazy {
        AuthoriseInteractor(miServices, messageRepo, executionThreads)
    }

    val observeBluetoothInteractor: ObserveBluetoothInteractor by lazy {
        ObserveBluetoothInteractor(rxBleClient, messageRepo, executionThreads)
    }

    val isBluetoothOnInteractor: IsBluetoothOnInteractor by lazy {
        IsBluetoothOnInteractor(bleManager, executionThreads)
    }

    val cleanupBluetoothOnInteractor: CleanupBluetoothInteractor by lazy {
        CleanupBluetoothInteractor(bleManager, executionThreads)
    }

    val disconnectDeviceInteractor: DisconnectDeviceInteractor by lazy {
        DisconnectDeviceInteractor(bleManager, executionThreads)
    }

    val cancelScanInteractor: CancelScanInteractor by lazy {
        CancelScanInteractor(bleManager, messageRepo, executionThreads)
    }

    val stopNotifyHeartServiceInteractor: StopNotifyHeartServiceInteractor by lazy {
        StopNotifyHeartServiceInteractor(bleManager, miServices, messageRepo, executionThreads)
    }

    val startNotifyHeartServiceInteractor: StartNotifyHeartServiceInteractor by lazy {
        StartNotifyHeartServiceInteractor(bleManager, miServices, messageRepo, executionThreads)
    }

    val scanDevicesInteractor: ScanForDevicesInteractor by lazy {
        ScanForDevicesInteractor(bleManager, messageRepo, executionThreads)
    }

    val connectToMacInteractor: ConnectToMacInteractor by lazy {
        ConnectToMacInteractor(rxBleClient, miServices, messageRepo, executionThreads)
    }

    val observeConnectionInteractor: ObserveConnectionInteractor by lazy {
        ObserveConnectionInteractor(rxBleClient, messageRepo, executionThreads)
    }

    val measureSingleHeartRateInteractor: MeasureSingleHeartRateInteractor by lazy {
        MeasureSingleHeartRateInteractor(miServices, messageRepo, executionThreads)
    }

    val getBondedDevicesInteractor: GetBondedDevicesInteractor by lazy {
        GetBondedDevicesInteractor(bleManager, executionThreads)
    }

    val pingHeartRateInteractor: PingHeartRateInteractor by lazy {
        PingHeartRateInteractor(bleManager, miServices, messageRepo, executionThreads)
    }

    val stopHeartRateMeasurementInteractor: StopHeartRateMeasurementInteractor by lazy {
        StopHeartRateMeasurementInteractor(bleManager, miServices, messageRepo, executionThreads)
    }

}