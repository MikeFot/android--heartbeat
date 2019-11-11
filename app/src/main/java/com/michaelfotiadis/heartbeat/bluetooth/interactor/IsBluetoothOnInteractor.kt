package com.michaelfotiadis.heartbeat.bluetooth.interactor

import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import kotlinx.coroutines.withContext

class IsBluetoothOnInteractor(
    private val bleManager: BleManager,
    private val executionThreads: ExecutionThreads
) {

    suspend fun execute(): Boolean {
        return withContext(executionThreads.jobScope.coroutineContext) {
            bleManager.isBlueEnable
        }
    }
}