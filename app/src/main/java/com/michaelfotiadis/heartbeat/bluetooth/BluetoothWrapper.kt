package com.michaelfotiadis.heartbeat.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.data.BleScanState
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "BLE"

class BluetoothWrapper(
    private val application: Application,
    private val appLogger: AppLogger
) {

    private val bleManager = BleManager.getInstance()

    init {
        bleManager.run {
            init(application)
            enableLog(true)
            initScanRule(
                BleScanRuleConfig.Builder()
                    .setScanTimeOut(TimeUnit.SECONDS.toMillis(10))
                    .build()
            )
            appLogger.get(TAG).d("Bluetooth initialised")
        }
    }

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

    fun getBondedDevices(): Set<BluetoothDevice> {
        return bleManager.bluetoothAdapter.bondedDevices ?: setOf()
    }

    @Throws(IllegalArgumentException::class)
    fun connectToMac(
        macAddress: String,
        callback: (ConnectionStatus) -> Unit
    ) {
        bleManager.connect(macAddress, object : BleGattCallback() {
            override fun onStartConnect() {
                appLogger.get(TAG).d("Connection Started")
                callback.invoke(ConnectionStatus.Started)
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                appLogger.get(TAG).d("Connection Terminated")
                callback.invoke(
                    ConnectionStatus.Disconnected(
                        isActiveDisConnected,
                        device,
                        gatt,
                        status
                    )
                )
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                appLogger.get(TAG).d("Connection Success")
                callback.invoke(ConnectionStatus.Connected(bleDevice, gatt, status))
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                appLogger.get(TAG).d("Connection Failed")
                callback.invoke(ConnectionStatus.Failed(bleDevice, exception))
            }
        })
    }

    fun scan(callback: (ScanStatus) -> Unit) {
        appLogger.get(TAG).d("Scanning")
        val devices = mutableListOf<BleDevice>()
        bleManager.scan(object : BleScanCallback() {
            override fun onScanFinished(scanResultList: List<BleDevice>?) {
                appLogger.get(TAG).d("Finished with ${scanResultList?.size} items")
                callback.invoke(ScanStatus.Finished(scanResultList ?: listOf()))
            }

            override fun onScanStarted(success: Boolean) {
                appLogger.get(TAG).d("Scan started $success")
                callback.invoke(ScanStatus.Started)
            }

            override fun onScanning(bleDevice: BleDevice?) {
                appLogger.get(TAG).d("Scanning device ${bleDevice?.device?.uuids}")
                if (bleDevice != null) {
                    devices.add(bleDevice)
                    callback.invoke(ScanStatus.Scanning(devices))
                }
            }
        })
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
        bleManager.notify(
            bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC.toString(),
            object : BleNotifyCallback() {
                override fun onCharacteristicChanged(data: ByteArray?) {
                    val message = Arrays.toString(data) ?: ""
                    appLogger.get(TAG).d("DATA: $message")
                    if (data != null && data.size >= 2) {
                        val heartRate = data[1]
                        callback.invoke(HeartRateStatus.Updated(heartRate.toInt()))
                    }
                }

                override fun onNotifyFailure(exception: BleException?) {
                    appLogger.get(TAG).e("EX: ${exception?.description}")
                    callback.invoke(HeartRateStatus.Failed(exception))
                }

                override fun onNotifySuccess() {
                    appLogger.get(TAG).d("Notify success")
                    callback.invoke(HeartRateStatus.Success)
                }
            })
    }

    fun stopNotifyHeartService(bleDevice: BleDevice) {
        bleManager.stopNotify(
            bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC.toString()
        )
        appLogger.get(TAG).d("Stop Notify")
    }

    fun cancelScan() {
        if (bleManager.scanSate == BleScanState.STATE_SCANNING) {
            appLogger.get(TAG).d("Scan cancelled")
            bleManager.cancelScan()
        }
    }

    fun disconnect(bleDevice: BleDevice) {
        bleManager.disconnect(bleDevice)
    }
}
