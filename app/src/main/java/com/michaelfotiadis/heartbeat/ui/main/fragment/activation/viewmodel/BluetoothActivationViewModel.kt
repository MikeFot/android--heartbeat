package com.michaelfotiadis.heartbeat.ui.main.fragment.activation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import javax.inject.Inject

class BluetoothActivationViewModel(
    bluetoothStatusProvider: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val actionLiveData: LiveData<Action> = Transformations.map(
        bluetoothStatusProvider.bluetoothConnectionLiveData
    ) { enabled ->
        if (enabled) {
            Action.MOVE_TO_NEXT
        } else {
            Action.BLUETOOTH_UNAVAILABLE
        }
    }

    fun checkConnection() {
        intentDispatcher.checkConnection()
    }

    fun enableBluetooth() {
        intentDispatcher.askToEnableBluetooth()
    }
}

enum class Action {
    BLUETOOTH_UNAVAILABLE,
    MOVE_TO_NEXT
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
