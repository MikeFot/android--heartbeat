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
import com.michaelfotiadis.heartbeat.bluetooth.constants.UUIDs
import com.michaelfotiadis.heartbeat.bluetooth.factory.BluetoothInteractorFactory
import com.michaelfotiadis.heartbeat.bluetooth.interactor.Cancellable
import com.michaelfotiadis.heartbeat.bluetooth.interactor.ExecuteAuthorisationSequenceInteractor
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.MessageRepo

class BluetoothHandler(
    private val context: Context,
    private val bluetoothRepo: BluetoothRepo,
    private val messageRepo: MessageRepo,
    private val factory: BluetoothInteractorFactory
) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothGatt: BluetoothGatt? = null

    private val operations = mutableListOf<Cancellable>()

    private var heartRateOperation: Cancellable? = null
    private var scanCancellable: Cancellable? = null

    private val callback = GattCallback()

    private fun updateConnectionState(newState: Int, gatt: BluetoothGatt) {
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


    fun discoverServices() {
        bluetoothGatt?.discoverServices()
    }

    fun notifyAuthorisation() {
        val gatt = bluetoothGatt
        if (gatt != null) {
            factory.authoriseMiBandInteractor.execute(gatt)
        } else {
            messageRepo.logError("GATT IS NULL!")
            bluetoothRepo.postAction(BluetoothRepo.Action.Disconnected(""))
        }
    }

    fun executeAuthorisationSequence() {
        val gatt = bluetoothGatt
        if (gatt != null) {
            factory.executeAuthorisationSequenceInteractor.execute(gatt).also { result ->
                val action = when (result) {
                    ExecuteAuthorisationSequenceInteractor.Result.STEP_1 -> BluetoothRepo.Action.AuthorisationStepOne
                    ExecuteAuthorisationSequenceInteractor.Result.STEP_2 -> BluetoothRepo.Action.AuthorisationStepTwo
                    ExecuteAuthorisationSequenceInteractor.Result.DONE -> BluetoothRepo.Action.AuthorisationComplete
                    ExecuteAuthorisationSequenceInteractor.Result.ERROR -> BluetoothRepo.Action.AuthorisationFailed
                }
                bluetoothRepo.postAction(action)
            }
        } else {
            bluetoothRepo.postAction(BluetoothRepo.Action.Disconnected(""))
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
        messageRepo.log("Attempting connection to mac $macAddress")
        bluetoothRepo.postAction(BluetoothRepo.Action.Idle)
        try {
            val device = bluetoothManager.adapter.getRemoteDevice(macAddress)
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                device.createBond()
                messageRepo.log("Created bond with device")
            }
            bluetoothRepo.connectedMacAddress = macAddress
            bluetoothGatt = device.connectGatt(context, true, callback)
        } catch (exception: IllegalArgumentException) {
            messageRepo.logError("Exception connecting")
            bluetoothRepo.postAction(BluetoothRepo.Action.ConnectionFailed)
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

    fun disconnect() {
        messageRepo.log("Disconnecting")
        bluetoothGatt?.disconnect()
        bluetoothGatt = null
        stopOperations()
    }

    private fun stopOperations() {
        messageRepo.log("Stopping Operations")
        operations.forEach { cancellable -> cancellable.cancel() }
        operations.clear()
    }

    fun cleanup() {
        messageRepo.log("Cleanup")
        factory.cleanupBluetoothOnInteractor.execute()
        stopOperations()
    }

    private fun register(cancellable: Cancellable) {
        operations.add(cancellable)
    }

    inner class GattCallback : BluetoothGattCallback() {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            messageRepo.log("Characteristic changed ${characteristic?.uuid}")
            when (characteristic?.uuid.toString()) {
                UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC_STRING -> {
                    messageRepo.log("AUTH characteristic changed '${characteristic?.value}'")
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
            when (characteristic.uuid.toString()) {
                UUIDs.CUSTOM_SERVICE_AUTH_CHARACTERISTIC_STRING -> {
                    messageRepo.log("AUTH characteristic changed '${characteristic.value}'")
                    bluetoothRepo.postAction(BluetoothRepo.Action.AuthorisationNotified)
                }
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            updateConnectionState(newState, gatt)
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
            messageRepo.log("onMtuChanged $mtu")
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
            messageRepo.log("onPhyRead $rxPhy")
        }

        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
            messageRepo.log("onPhyUpdate $rxPhy")
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            messageRepo.log("RSSI $rssi")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            bluetoothRepo.postAction(BluetoothRepo.Action.ServicesDiscovered)
        }
    }

}
