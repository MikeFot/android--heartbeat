package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.factory.BluetoothInteractorFactory
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.logger.AppLogger

private const val TAG = "BLE"

class BluetoothWrapper(
    private val bleManager: BleManager,
    private val factory: BluetoothInteractorFactory,
    private val appLogger: AppLogger
) {

    private val operations = mutableListOf<Cancellable>()
    private var heartRateOperation: Cancellable? = null
    private var continuousHeartRateOperation: Cancellable? = null

    fun isBluetoothEnabled(): Boolean {
        return bleManager.bluetoothAdapter.isEnabled
    }

    fun askToEnableBluetooth(context: Context) {
        if (!isBluetoothEnabled()) {
            context.startActivity(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    suspend fun getBondedDevices(): Set<BluetoothDevice> {
        return factory.getBondedDevicesInteractor.execute()
    }

    fun connectToMac(macAddress: String, callback: (ConnectionStatus) -> Unit) {
        stopOperations()
        factory.connectToMacInteractor
            .execute(macAddress, callback)
            .register(operations)
    }

    fun scan(callback: (ScanStatus) -> Unit) {
        stopOperations()
        appLogger.get(TAG).d("Scanning")
        factory.scanDevicesInteractor
            .execute(callback)
            .register(operations)
    }

    fun startPingHeartRate(bleDevice: BleDevice) {
        heartRateOperation?.cancel()
        heartRateOperation = factory.pingHeartRateInteractor.execute(bleDevice)
            .also { cancellable -> operations.add(cancellable) }
    }

    fun stopPingHeartRate() {
        heartRateOperation?.cancel()
        heartRateOperation = null
    }

    fun askForSingleHeartRate(bleDevice: BleDevice?, callback: (HeartRateStatus) -> Unit) {
        if (bleDevice == null) {
            callback.invoke(HeartRateStatus.Failed("No device connected"))
        } else {
            factory.measureSingleHeartRateInteractor.execute(bleDevice, callback)
                .register(operations)
        }
    }

    fun stopContinuousHeartRate(bleDevice: BleDevice, callback: () -> Unit) {
        if (continuousHeartRateOperation != null) {
            factory.stopHeartRateMeasurementInteractor
                .execute(bleDevice, callback)
                .register(operations)
        } else {
            callback.invoke()
        }
    }

    private fun startNotifyHeartService(bleDevice: BleDevice, callback: (HeartRateStatus) -> Unit) {
        factory.startNotifyHeartServiceInteractor
            .execute(bleDevice, callback)
            .register(operations)
    }

    fun stopNotifyHeartService(bleDevice: BleDevice) {
        factory.stopNotifyHeartServiceInteractor
            .execute(bleDevice)
            .register(operations)
    }

    fun cancelScan() {
        appLogger.get(TAG).d("Cancel Scan")
        factory.cancelScanInteractor
            .execute()
            .register(operations)
    }

    fun disconnect(bleDevice: BleDevice) {
        appLogger.get(TAG).d("Disconnect Device '${bleDevice.name}'")
        factory.disconnectDeviceInteractor
            .execute(bleDevice)
            .register(operations)
    }

    fun stopOperations() {
        operations.forEach { cancellable -> cancellable.cancel() }
        operations.clear()
    }
}
