package com.michaelfotiadis.heartbeat.ui.main.fragment.activation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import javax.inject.Inject

class BluetoothActivationViewModel(
    private val repo: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val actionLiveData: LiveData<Action> = Transformations.map(
        repo.bluetoothConnectionLiveData
    ) { enabled ->
        if (enabled) {
            val storedMac = repo.connectedMacAddress
            if (storedMac.isNullOrBlank()) {
                Action.MoveToBondedDevices
            } else {
                Action.MoveToPair(storedMac)
            }
        } else {
            Action.BluetoothUnavailable
        }
    }

    fun checkConnection() {
        intentDispatcher.checkConnection()
    }

    fun enableBluetooth() {
        intentDispatcher.askToEnableBluetooth()
    }
}

sealed class Action {
    object BluetoothUnavailable : Action()
    object MoveToBondedDevices : Action()
    data class MoveToPair(val mac: String) : Action()
}

class BluetoothActivationViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BluetoothActivationViewModel(
            bluetoothStatusProvider,
            intentDispatcher
        ) as T
    }
}
