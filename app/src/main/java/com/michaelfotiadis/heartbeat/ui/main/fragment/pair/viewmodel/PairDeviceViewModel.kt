package com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import javax.inject.Inject

class PairDeviceViewModel(
    private val bluetoothStatusProvider: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val connectionResultLiveData = MediatorLiveData<Action>().apply {
        addSource(bluetoothStatusProvider.actionLiveData) { bluetoothAction ->
            val action = when (bluetoothAction) {
                BluetoothRepo.Action.Idle -> Action.IDLE
                BluetoothRepo.Action.Connecting -> Action.CONNECTING
                is BluetoothRepo.Action.Connected -> Action.CONNECTED
                is BluetoothRepo.Action.Disconnected -> Action.DISCONNECTED
                BluetoothRepo.Action.ConnectionFailed -> Action.CONNECTION_FAILED
                BluetoothRepo.Action.ServicesDiscovered -> Action.SERVICES_DISCOVERED
                BluetoothRepo.Action.AuthorisationNotified -> Action.AUTH_NOTIFIED
                BluetoothRepo.Action.AuthorisationStepOne -> Action.AUTH_STEP_ONE
                BluetoothRepo.Action.AuthorisationStepTwo -> Action.AUTH_STEP_TWO
                BluetoothRepo.Action.AuthorisationFailed -> Action.AUTH_FAILED
                BluetoothRepo.Action.AuthorisationComplete -> Action.AUTH_DONE
            }
            postValue(action)
        }.apply { postValue(Action.IDLE) }
    }

    fun connect(macAddress: String) {
        intentDispatcher.connectToMacAddress(macAddress)
    }
}

enum class Action {
    IDLE,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    CONNECTION_FAILED,
    SERVICES_DISCOVERED,
    AUTH_NOTIFIED,
    AUTH_STEP_ONE,
    AUTH_STEP_TWO,
    AUTH_DONE,
    AUTH_FAILED
}

class PairDeviceViewModelFactory @Inject constructor(
    private val bluetoothStatusProvider: BluetoothRepo,
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
