package com.michaelfotiadis.heartbeat.bluetooth.factory

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.interactor.AuthoriseMiBandInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.CancelScanInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.CleanupBluetoothInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ExecuteAuthorisationSequenceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.GetBondedDevicesInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.PingHeartRateInteractor
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ScanForDevicesInteractor
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.MessageRepo

class BluetoothInteractorFactory(
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