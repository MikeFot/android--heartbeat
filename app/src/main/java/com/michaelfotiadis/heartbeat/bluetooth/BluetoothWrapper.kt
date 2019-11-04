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
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.data.BleScanState
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
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

    fun checkHeartServiceExists(bleDevice: BleDevice) {
        bleManager.read(bleDevice,
            UUIDs.HEART_RATE_SERVICE.toString(),
            UUIDs.HEART_RATE_MEASUREMENT_CHARACTERISTIC.toString(),
            object : BleReadCallback() {
                override fun onReadSuccess(data: ByteArray?) {
                    appLogger.get(TAG).d("DATA: $data")
                }

                override fun onReadFailure(exception: BleException?) {
                    appLogger.get(TAG).e("EX: ${exception?.description}")
                }
            })
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
