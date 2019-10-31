package com.michaelfotiadis.heartbeat.bluetooth

import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.data.BleScanState
import com.clj.fastble.scan.BleScanRuleConfig
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


private const val REQUEST_ENABLE_BT = 407
private const val TAG = "BLE"

class BluetoothWrapper(
    private val application: Application,
    private val dispatcher: CoroutineDispatcher,
    private val appLogger: AppLogger,
    private val bluetoothAdapter: BluetoothAdapter
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
        return bluetoothAdapter.isEnabled
    }

    fun askToEnableBluetooth(activity: Activity) {
        if (!isBluetoothEnabled()) {
            activity.startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        }
    }

    fun askToEnableBluetooth(fragment: Fragment) {
        if (!isBluetoothEnabled()) {
            fragment.startActivityForResult(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                REQUEST_ENABLE_BT
            )
        }
    }

    suspend fun getBondedDevices(): Set<BluetoothDevice> {
        return withContext(dispatcher) {
            bluetoothAdapter.bondedDevices ?: setOf()
        }
    }

    suspend fun scan(callback: (devices: List<BleDevice>) -> Unit) {
        appLogger.get(TAG).d("Scanning")
        getBondedDevices()
        bleManager.scan(object : BleScanCallback() {
            override fun onScanFinished(scanResultList: List<BleDevice>?) {
                appLogger.get(TAG).d("Finished with ${scanResultList?.size} items")
                callback.invoke(scanResultList ?: listOf())
            }

            override fun onScanStarted(success: Boolean) {
                appLogger.get(TAG).d("Scan started $success")
            }

            override fun onScanning(bleDevice: BleDevice?) {
                appLogger.get(TAG).d("Scanning device ${bleDevice?.device?.uuids}")
            }
        })
    }

    suspend fun cancelScan() {
        withContext(dispatcher) {
            if (bleManager.scanSate == BleScanState.STATE_SCANNING) {
                appLogger.get(TAG).d("Scan cancelled")
                bleManager.cancelScan()
            }
        }
    }

    suspend fun disconnect() {
        withContext(dispatcher) {
            bleManager.enableBluetooth()
        }
    }
}