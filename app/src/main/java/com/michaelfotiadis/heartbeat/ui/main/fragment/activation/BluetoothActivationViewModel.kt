package com.michaelfotiadis.heartbeat.ui.main.fragment.activation

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import com.polidea.rxandroidble2.RxBleClient
import javax.inject.Inject

class BluetoothActivationViewModel(
    bluetoothStatusProvider: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val actionLiveData: LiveData<Action> = Transformations.map(
        bluetoothStatusProvider.bluetoothStateLiveData
    ) { state ->
        when (state) {
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE,
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> Action.BLUETOOTH_UNAVAILABLE
            null -> Action.BLUETOOTH_UNAVAILABLE
            else -> Action.MOVE_TO_NEXT
        }
    }

    fun checkStatus() {
        intentDispatcher.checkBluetoothConnection()
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
        return BluetoothActivationViewModel(bluetoothStatusProvider, intentDispatcher) as T
    }
}
