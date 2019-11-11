package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.factory.BluetoothInteractorFactory
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo

class BluetoothWrapper(
    private val bluetoothRepo: BluetoothRepo,
    private val factory: BluetoothInteractorFactory
) {

    private var bluetoothGatt: BluetoothGatt? = null

    private val operations = mutableListOf<Cancellable>()

    private var stateCancellable: Cancellable? = null
    private var connectionCancellable: Cancellable? = null
    private var heartRateOperation: Cancellable? = null
    private var continuousHeartRateOperation: Cancellable? = null

    suspend fun isBluetoothEnabled(): Boolean {
        return factory.isBluetoothOnInteractor.execute()
    }

    suspend fun askToEnableBluetooth(context: Context) {
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

    fun observeBluetoothState() {
        stateCancellable?.cancel()
        stateCancellable = factory.observeBluetoothInteractor
            .execute(bluetoothRepo.bluetoothStateLiveData::postValue)
    }

    fun connectToMacInitial(macAddress: String) {
        stopOperations()
        connectionCancellable?.cancel()
        connectionCancellable = factory.connectToMacInteractor.execute(
            macAddress,
            true,
            bluetoothRepo.connectionStatusLiveData::postValue
        )
    }

    fun authorise() {
        val currentValue = bluetoothRepo.connectionStatusLiveData.value
        val connection = currentValue?.rxBleConnection
        if (currentValue != null && connection != null) {
            register(
                factory.authoriseInteractor.execute(
                    currentValue.rxBleDevice,
                    connection,
                    bluetoothRepo.connectionStatusLiveData::postValue
                )
            )
        } else {
            throw IllegalStateException("Null connection!")
        }
    }

    fun scan(callback: (ScanStatus) -> Unit) {
        stopOperations()
        register(
            factory.scanDevicesInteractor.execute(callback)
        )
    }

    fun startPingHeartRate(bleDevice: BleDevice) {
        heartRateOperation?.cancel()
        heartRateOperation = factory.pingHeartRateInteractor.execute(bleDevice)
            .also { cancellable -> register(cancellable) }
    }

    fun stopPingHeartRate() {
        heartRateOperation?.cancel()
        heartRateOperation = null
    }

    fun askForSingleHeartRate() {
        val currentValue = bluetoothRepo.connectionStatusLiveData.value
        val connection = currentValue?.rxBleConnection
        if (currentValue != null && connection != null) {
            register(
                factory.measureSingleHeartRateInteractor.execute(connection) {
                    bluetoothRepo.heartRateStatus::postValue
                }
            )
        } else {
            throw IllegalStateException("Null connection!")
        }
    }

    fun stopContinuousHeartRate(bleDevice: BleDevice, callback: () -> Unit) {
        if (continuousHeartRateOperation != null) {
            register(
                factory.stopHeartRateMeasurementInteractor.execute(bleDevice, callback)
            )
        } else {
            callback.invoke()
        }
    }

    fun startNotifyHeartService(bleDevice: BleDevice, callback: (HeartRateStatus) -> Unit) {
        register(
            factory.startNotifyHeartServiceInteractor.execute(bleDevice, callback)
        )
    }

    fun stopNotifyHeartService(bleDevice: BleDevice) {
        register(
            factory.stopNotifyHeartServiceInteractor.execute(bleDevice)
        )
    }

    fun cancelScan() {
        register(
            factory.cancelScanInteractor.execute()
        )
    }

    fun disconnect() {
        connectionCancellable?.cancel()
    }

    fun stopOperations() {
        /*operations.forEach { cancellable -> cancellable.cancel() }
        operations.clear()*/
    }

    fun cleanup() {
        factory.cleanupBluetoothOnInteractor.execute()
        stateCancellable?.cancel()
    }

    private fun register(cancellable: Cancellable) {
        operations.add(cancellable)
    }
}
