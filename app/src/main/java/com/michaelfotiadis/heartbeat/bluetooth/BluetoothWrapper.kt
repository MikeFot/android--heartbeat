package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.clj.fastble.data.BleDevice
import com.michaelfotiadis.heartbeat.bluetooth.constants.UUIDs
import com.michaelfotiadis.heartbeat.bluetooth.factory.BluetoothInteractorFactory
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ExecuteAuthorisationSequenceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import kotlinx.coroutines.launch

class BluetoothWrapper(
    private val context: Context,
    private val bluetoothRepo: BluetoothRepo,
    private val messageRepo: MessageRepo,
    private val executionThreads: ExecutionThreads,
    private val factory: BluetoothInteractorFactory
) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothGatt: BluetoothGatt? = null

    private val operations = mutableListOf<Cancellable>()

    private var stateCancellable: Cancellable? = null
    private var connectionCancellable: Cancellable? = null
    private var heartRateOperation: Cancellable? = null
    private var continuousHeartRateOperation: Cancellable? = null

    private val callback = object : BluetoothGattCallback() {
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            messageRepo.log("Characteristic changed ${characteristic.uuid}")
            when (characteristic.uuid.toString()) {
                UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC_STRING -> executionThreads.jobScope.launch {
                    messageRepo.log("AUTH characteristic changed '${characteristic.value}'")
                    bluetoothRepo.postAction(BluetoothRepo.Action.AuthorisationNotified)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            messageRepo.log("onCharacteristicRead ${characteristic.uuid}")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            messageRepo.log("onCharacteristicWrite ${characteristic.uuid}")
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            messageRepo.log("New State $newState")
            when (newState) {
                BluetoothGatt.STATE_CONNECTING ->
                    bluetoothRepo.postAction(BluetoothRepo.Action.Connecting)
                BluetoothGatt.STATE_CONNECTED -> {
                    messageRepo.log("Connected")
                    bluetoothRepo.postAction(
                        BluetoothRepo.Action.Connected(
                            gatt.device.name
                        )
                    )
                }
                BluetoothGatt.STATE_DISCONNECTED -> {
                    messageRepo.log("Disconnected")
                    bluetoothRepo.connectedMacAddress = null
                    bluetoothRepo.postAction(
                        BluetoothRepo.Action.Disconnected(
                            gatt.device.name
                        )
                    )
                }
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
            messageRepo.log("onDescriptorRead ${descriptor.uuid}")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            messageRepo.log("onDescriptorWrite ${descriptor.uuid}")
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
        }

        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            messageRepo.log("RSSI $rssi")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            bluetoothRepo.servicesDiscovered.postValue(true)
            bluetoothRepo.postAction(BluetoothRepo.Action.ServicesDiscovered)

        }
    }


    fun discoverServices() {
        executionThreads.jobScope.launch {
            bluetoothGatt?.discoverServices()
        }
    }

    fun notifyAuthorisation() {
        bluetoothGatt?.let { gatt ->
            if (bluetoothRepo.authorisationAttempted.value == false) {
                executionThreads.jobScope.launch {
                    factory.authoriseMiBandInteractor.execute(gatt)
                    bluetoothRepo.authorisationAttempted.postValue(true)
                }
            } else {
                bluetoothRepo.postAction(BluetoothRepo.Action.AuthorisationComplete)
            }
        }
    }

    fun executeAuthorisationSequence() {
        bluetoothGatt?.let { gatt ->
            executionThreads.jobScope.launch {
                factory.executeAuthorisationSequenceInteractor.execute(gatt).also { result ->
                    val action = when (result) {
                        ExecuteAuthorisationSequenceInteractor.Result.STEP_1 -> BluetoothRepo.Action.AuthorisationStepOne
                        ExecuteAuthorisationSequenceInteractor.Result.STEP_2 -> BluetoothRepo.Action.AuthorisationStepTwo
                        ExecuteAuthorisationSequenceInteractor.Result.DONE -> BluetoothRepo.Action.AuthorisationComplete
                        ExecuteAuthorisationSequenceInteractor.Result.ERROR -> BluetoothRepo.Action.AuthorisationFailed
                    }
                    bluetoothRepo.postAction(action)
                }
            }
        }
    }

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
        messageRepo.log("Attempting connection to mac $macAddress")
        val device = bluetoothManager.adapter.getRemoteDevice(macAddress)
        bluetoothRepo.connectedMacAddress = macAddress
        bluetoothGatt = device.connectGatt(context, false, callback)
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

    fun startPingHeartRate() {
        heartRateOperation?.cancel()
        bluetoothGatt?.let { gatt ->
            heartRateOperation = factory.pingHeartRateInteractor.execute(gatt)
                .also { cancellable -> register(cancellable) }
        }
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
        bluetoothGatt?.disconnect()
        heartRateOperation?.cancel()
        bluetoothGatt = null
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
