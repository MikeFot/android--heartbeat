package com.michaelfotiadis.heartbeat.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.michaelfotiadis.heartbeat.bluetooth.constants.MiServices
import com.michaelfotiadis.heartbeat.bluetooth.constants.UUIDs
import com.michaelfotiadis.heartbeat.bluetooth.factory.BluetoothInteractorFactory
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ExecuteAuthSequenceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepoAction
import com.michaelfotiadis.heartbeat.repo.message.MessageRepo
import java.util.*

class BluetoothHandler(
    private val context: Context,
    private val bluetoothRepo: BluetoothRepo,
    private val messageRepo: MessageRepo,
    private val miServices: MiServices,
    private val factory: BluetoothInteractorFactory
) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private var bluetoothGatt: BluetoothGatt? = null

    private val operations = mutableListOf<Cancellable>()

    private var heartRateOperation: Cancellable? = null
    private var scanCancellable: Cancellable? = null

    private val bleServer =
        bluetoothManager.openGattServer(
            context.applicationContext,
            object : BluetoothGattServerCallback() {
            })

    private val callback = GattCallback()

    private fun updateConnectionState(newState: Int, gatt: BluetoothGatt) {
        messageRepo.log("New State $newState")
        when (newState) {
            BluetoothGatt.STATE_CONNECTING ->
                bluetoothRepo.postAction(BluetoothRepoAction.Connecting)
            BluetoothGatt.STATE_CONNECTED -> {
                messageRepo.log("Connected")
                bluetoothRepo.postAction(
                    BluetoothRepoAction.Connected(
                        gatt.device.name ?: "UNKNOWN"
                    )
                )
                getConnectedDevice()?.let { device ->
                    val isConnected = bleServer.connect(device, true)
                    messageRepo.log("Gatt Server $isConnected")
                }
            }
            BluetoothGatt.STATE_DISCONNECTED -> {
                messageRepo.log("Disconnected")
                bluetoothRepo.postAction(
                    BluetoothRepoAction.Disconnected(gatt.device.name ?: "UNKNOWN")
                )

                getConnectedDevice()?.let(bleServer::cancelConnection)
            }
        }
    }

    private fun getConnectedDevice(): BluetoothDevice? {
        return if (bluetoothRepo.connectedMacAddress.isNullOrBlank()) {
            null
        } else {
            bluetoothManager.adapter.getRemoteDevice(bluetoothRepo.connectedMacAddress)
        }
    }

    fun discoverServices() {
        bluetoothGatt?.discoverServices()
    }

    fun notifyAuthorisation() {
        val gatt = bluetoothGatt
        if (gatt != null) {
            val canAuthorise = factory.readAuthInteractor.execute(gatt)
            if (!canAuthorise) {
                messageRepo.logError("Auth Service unavailable")
                bluetoothRepo.connectedMacAddress = null
                bluetoothRepo.postAction(BluetoothRepoAction.AuthorisationFailed)
            }
        } else {
            messageRepo.logError("GATT IS NULL!")
            bluetoothRepo.postAction(BluetoothRepoAction.Disconnected(""))
        }
    }

    fun executeAuthorisationSequence() {
        val gatt = bluetoothGatt
        if (gatt != null) {
            messageRepo.log("Executing authorisation")
            factory.executeAuthSequenceInteractor.execute(gatt).also { result ->
                val action = when (result) {
                    ExecuteAuthSequenceInteractor.Result.STEP_1 -> BluetoothRepoAction.AuthorisationStepOne
                    ExecuteAuthSequenceInteractor.Result.STEP_2 -> BluetoothRepoAction.AuthorisationStepTwo
                    ExecuteAuthSequenceInteractor.Result.DONE -> BluetoothRepoAction.AuthorisationComplete
                    ExecuteAuthSequenceInteractor.Result.ERROR -> BluetoothRepoAction.AuthorisationFailed
                }
                bluetoothRepo.postAction(action)
            }
        } else {
            bluetoothRepo.postAction(BluetoothRepoAction.Disconnected(""))
        }
    }

    fun checkConnection() {
        bluetoothManager.adapter.isEnabled.also(bluetoothRepo.bluetoothConnectionLiveData::postValue)
    }

    fun askToEnableBluetooth(context: Context) {
        if (bluetoothRepo.bluetoothConnectionLiveData.value != true) {
            context.startActivity(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .addFlags(FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun getBondedDevices() {
        factory.getBondedDevicesInteractor.execute { results ->
            bluetoothRepo.bondedDevicesLiveData.postValue(results.toSet())
        }
    }

    fun connectToMacInitial(macAddress: String) {
        stopOperations()
        disconnect(false)
        messageRepo.log("Attempting connection to mac $macAddress")
        bluetoothRepo.postAction(BluetoothRepoAction.Idle)
        try {
            val device = bluetoothManager.adapter.getRemoteDevice(macAddress)
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
                messageRepo.log("Created bond with device")
            } else {
                messageRepo.log("Device already bonded")
            }
            bluetoothRepo.connectedMacAddress = macAddress
            bluetoothGatt = device.connectGatt(context, true, callback)
        } catch (exception: IllegalArgumentException) {
            messageRepo.logError("Exception connecting")
            bluetoothRepo.postAction(BluetoothRepoAction.ConnectionFailed)
        }
    }

    fun scan(callback: (ScanStatus) -> Unit) {
        messageRepo.log("Start Scan")
        stopOperations()
        scanCancellable = factory.scanDevicesInteractor.execute(callback).apply { register(this) }
    }

    fun cancelScan() {
        messageRepo.log("Cancel Scan")
        scanCancellable?.cancel()
        register(
            factory.cancelScanInteractor.execute()
        )
    }

    suspend fun refreshDeviceInfo() {
        val gatt = bluetoothGatt
        if (gatt != null) {
            messageRepo.log("Refreshing device info")
            factory.refreshDeviceInfoInteractor.execute(gatt)
        } else {
            throw IllegalStateException("Null gatt!")
        }
    }

    fun startPingHeartRate() {
        messageRepo.log("Start Ping Heart Rate")
        heartRateOperation?.cancel()
        bluetoothGatt?.let { gatt ->
            heartRateOperation = factory.pingHeartRateInteractor.execute(gatt)
                .apply { register(this) }
        }
    }

    fun stopPingHeartRate() {
        messageRepo.log("Stop Ping Heart Rate")
        heartRateOperation?.cancel()
        heartRateOperation = null
    }

    fun disconnect(forget: Boolean) {
        if (forget) {
            bluetoothRepo.connectedMacAddress = null
        }
        bluetoothGatt?.disconnect()
        stopOperations()
        messageRepo.log("Disconnected $forget")
    }

    private fun stopOperations() {
        messageRepo.log("Stopping ${operations.size} Operations")
        operations.forEach { cancellable -> cancellable.cancel() }
        operations.clear()
        factory.cleanupBluetoothOnInteractor.execute()
    }

    fun cleanup() {
        messageRepo.log("Cleanup")
        disconnect(false)
        bluetoothGatt?.close()
        bleServer.close()
    }

    private fun register(cancellable: Cancellable) {
        operations.add(cancellable)
    }

    inner class GattCallback : BluetoothGattCallback() {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            messageRepo.log("Characteristic changed ${characteristic.uuid}")
            when (characteristic.uuid.toString()) {
                UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC_STRING -> {
                    messageRepo.log("AUTH characteristic changed '${characteristic.value}'")
                    bluetoothRepo.postAction(BluetoothRepoAction.AuthorisationNotified)
                }
            }
            factory.updateCharacteristicInteractor.execute(characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            messageRepo.log(
                "onCharacteristicRead ${characteristic.uuid} - ${Arrays.toString(
                    characteristic.value
                )}"
            )

            if (characteristic.uuid == miServices.authService.authCharacteristic) {
                val isAuthorised = miServices.isAuthorised(characteristic.value)
                messageRepo.log("Is authorised: $isAuthorised")
                if (isAuthorised) {
                    executeAuthorisationSequence()
                } else {
                    factory.notifyAuthInteractor.execute(gatt)
                }
            }
            factory.updateCharacteristicInteractor.execute(characteristic)
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
            updateConnectionState(newState, gatt)
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            messageRepo.log("On Services Discovered")
            bluetoothRepo.postAction(BluetoothRepoAction.ServicesDiscovered)
        }
    }
}
