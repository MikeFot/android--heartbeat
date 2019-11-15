package com.michaelfotiadis.heartbeat.bluetooth.factory

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.interactor.CancelScanInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.CleanupBluetoothInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ExecuteAuthSequenceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.GetBondedDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.NotifyAuthInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.PingHeartRateInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ReadAuthInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.RefreshDeviceInfoInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ScanForDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.UpdateCharacteristicInteractor
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo

class BluetoothInteractorFactory(
    private val bleManager: BleManager,
    private val bluetoothRepo: BluetoothRepo,
    private val miServices: MiServices,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads
) {

    val readAuthInteractor: ReadAuthInteractor by lazy {
        ReadAuthInteractor(miServices)
    }

    val notifyAuthInteractor: NotifyAuthInteractor by lazy {
        NotifyAuthInteractor(miServices, messageRepo)
    }

    val executeAuthSequenceInteractor: ExecuteAuthSequenceInteractor by lazy {
        ExecuteAuthSequenceInteractor(miServices, messageRepo)
    }

    val getBondedDevicesInteractor: GetBondedDevicesInteractor by lazy {
        GetBondedDevicesInteractor(bleManager, executionThreads)
    }

    val pingHeartRateInteractor: PingHeartRateInteractor by lazy {
        PingHeartRateInteractor(
            miServices,
            messageRepo,
            executionThreads
        )
    }

    val refreshDeviceInfoInteractor: RefreshDeviceInfoInteractor by lazy {
        RefreshDeviceInfoInteractor(miServices, bluetoothRepo, messageRepo)
    }

    val updateCharacteristicInteractor: UpdateCharacteristicInteractor by lazy {
        UpdateCharacteristicInteractor(miServices, bluetoothRepo, messageRepo)
    }

    val cleanupBluetoothOnInteractor: CleanupBluetoothInteractor by lazy {
        CleanupBluetoothInteractor(bleManager, executionThreads)
    }

    val cancelScanInteractor: CancelScanInteractor by lazy {
        CancelScanInteractor(bleManager, executionThreads)
    }

    val scanDevicesInteractor: ScanForDevicesInteractor by lazy {
        ScanForDevicesInteractor(
            bleManager,
            messageRepo,
            executionThreads
        )
    }
}