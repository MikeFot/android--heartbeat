package com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.bluetooth.BluetoothRepoAction
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@UseExperimental(InternalCoroutinesApi::class)
class PairDeviceViewModel(
    private val repo: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val connectionResultLiveData: LiveData<Action>

    init {
        connectionResultLiveData = LiveDataReactiveStreams.fromPublisher(
            repo.actionAsFlowable()
                .map { bluetoothAction ->
                    return@map when (bluetoothAction) {
                        BluetoothRepoAction.Idle -> Action.IDLE
                        BluetoothRepoAction.Connecting -> Action.CONNECTING
                        is BluetoothRepoAction.Connected -> Action.CONNECTED
                        is BluetoothRepoAction.Disconnected -> Action.DISCONNECTED
                        BluetoothRepoAction.ConnectionFailed -> Action.CONNECTION_FAILED
                        BluetoothRepoAction.ServicesDiscovered -> Action.SERVICES_DISCOVERED
                        BluetoothRepoAction.AuthorisationNotified -> Action.AUTH_NOTIFIED
                        BluetoothRepoAction.AuthorisationStepOne -> Action.AUTH_STEP_ONE
                        BluetoothRepoAction.AuthorisationStepTwo -> Action.AUTH_STEP_TWO
                        BluetoothRepoAction.AuthorisationFailed -> Action.AUTH_FAILED
                        BluetoothRepoAction.AuthorisationComplete -> Action.AUTH_DONE
                    }
                }
        )
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
