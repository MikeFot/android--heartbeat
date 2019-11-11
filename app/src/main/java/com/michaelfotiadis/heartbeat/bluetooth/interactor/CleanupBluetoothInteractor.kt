package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import kotlinx.coroutines.launch

class CleanupBluetoothInteractor(
    private val bleManager: BleManager,
    private val executionThreads: ExecutionThreads
) {

    fun execute() {
        executionThreads.jobScope.launch {
            bleManager.destroy()
        }
    }
}