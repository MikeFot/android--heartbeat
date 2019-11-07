package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiMessages
import com.michaelfotiadis.heartbeat.bluetooth.constants.UUIDs
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

    fun pingHeartRateControl(bleDevice: BleDevice) {
        bleManager.write(bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_CONTROL_POINT_CHARACTERISTIC.toString(),
            MiMessages.HMC_PING,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    appLogger.get(TAG).d("Ping Write successful")
                }

                override fun onWriteFailure(exception: BleException?) {
                    appLogger.get(TAG).e("Ping Write failed")
                }
            })
    }

    fun stopHeartRate(bleDevice: BleDevice, callback: () -> Unit) {
        bleManager.write(bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_CONTROL_POINT_CHARACTERISTIC.toString(),
            byteArrayOf(21, 2, 1),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    appLogger.get(TAG).d("Stop Heart Rate Write successful")
                    callback.invoke()
                }

                override fun onWriteFailure(exception: BleException?) {
                    appLogger.get(TAG).e("Stop Heart Rate Write failed: ${exception?.description}")
                    callback.invoke()
                }
            })
    }

    fun askForSingleHeartRate(bleDevice: BleDevice, callback: (HeartRateStatus) -> Unit) {

        bleManager.write(bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_CONTROL_POINT_CHARACTERISTIC.toString(),
            MiMessages.HMC_SINGLE_MEASUREMENT,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    appLogger.get(TAG).d("Single Heart Rate Write successful")
                    startNotifyHeartService(bleDevice, callback)
                }

                override fun onWriteFailure(exception: BleException?) {
                    appLogger.get(TAG)
                        .e("Single Heart Rate Write failed: ${exception?.description}")
                    callback.invoke(HeartRateStatus.Failed(exception))
                    stopNotifyHeartService(bleDevice)
                }
            })
    }

    fun askForContinuousHeartRate(bleDevice: BleDevice, callback: (HeartRateStatus) -> Unit) {
        bleManager.write(bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_CONTROL_POINT_CHARACTERISTIC.toString(),
            MiMessages.HMC_CONTINUOUS_MEASUREMENT,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    appLogger.get(TAG).d("Continuous Heart Rate Write successful")
                    startNotifyHeartService(bleDevice, callback)
                }

                override fun onWriteFailure(exception: BleException?) {
                    appLogger.get(TAG).e("Continuous Heart Rate Write failed")
                    callback.invoke(HeartRateStatus.Failed(exception))
                    stopNotifyHeartService(bleDevice)
                }
            })
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
