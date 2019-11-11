package com.michaelfotiadis.heartbeat.bluetooth.interactor.review

import android.bluetooth.BluetoothDevice
import com.clj.fastble.BleManager
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import kotlinx.coroutines.withContext

class GetBondedDevicesInteractor(
    private val bleManager: BleManager,
    private val executionThreads: ExecutionThreads
) {

    suspend fun execute(): MutableSet<BluetoothDevice> {
        return withContext(executionThreads.jobScope.coroutineContext) {
            bleManager.bluetoothAdapter.bondedDevices ?: setOf()
        }
    }
}