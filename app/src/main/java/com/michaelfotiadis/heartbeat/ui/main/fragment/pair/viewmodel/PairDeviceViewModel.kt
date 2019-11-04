package com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import javax.inject.Inject

class PairDeviceViewModel(
    private val bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val connectionResultLiveData =
        Transformations.map(bluetoothStatusProvider.connectionStatusLiveData, this::mapAction)

    private fun mapAction(connectionStatus: ConnectionStatus?): Action {
        return when (connectionStatus) {
            ConnectionStatus.Started -> Action.Started
            is ConnectionStatus.Connected -> Action.Connected(
                connectionStatus.bleDevice?.mac
            )
            is ConnectionStatus.Disconnected -> Action.Disconnected(
                connectionStatus.device?.mac
            )
            is ConnectionStatus.Failed -> Action.Failed(
                connectionStatus.exception?.description ?: ""
            )
            else -> Action.Idle
        }
    }

    fun connect(macAddress: String) {
        intentDispatcher.connectToMacAddress(macAddress)
    }

    fun checkSerial() {
        intentDispatcher.checkSerial()
    }
}

sealed class Action {
    object Idle : Action()
    object Started : Action()
    data class Failed(val message: String) : Action()
    data class Disconnected(val mac: String?) : Action()
    data class Connected(val mac: String?) : Action()
}

class PairDeviceViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothStatusProvider,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PairDeviceViewModel(
            bluetoothStatusProvider,
            intentDispatcher
        ) as T
    }
}
