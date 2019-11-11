package com.michaelfotiadis.heartbeat.ui.main.fragment.pair.viewmodel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.service.BluetoothServiceDispatcher
import javax.inject.Inject

class PairDeviceViewModel(
    private val bluetoothStatusProvider: BluetoothRepo,
    private val intentDispatcher: BluetoothServiceDispatcher
) : ViewModel() {

    val connectionResultLiveData = MediatorLiveData<Action>().apply {
        addSource(bluetoothStatusProvider.connectionStatusLiveData) { connectionStatus ->
            postValue(mapConnectionStatus(connectionStatus))
        }
        addSource(bluetoothStatusProvider.heartRateExists) { exists ->
            postValue(
                if (exists) {
                    Action.HeartRateNotified
                } else {
                    Action.HeartRateFailed("Could not init heart service")
                }
            )
        }
        addSource(bluetoothStatusProvider.heartRateStatus) { heartRateStatus ->
            postValue(mapHeartRate(heartRateStatus))
        }
    }

    private fun mapConnectionStatus(connectionStatus: ConnectionStatus?): Action {
        return when (connectionStatus) {
            is ConnectionStatus.Connected -> Action.ConnectionConnected(
                connectionStatus.rxBleDevice.macAddress
            )
            is ConnectionStatus.ConnectedNoHeartRate -> Action.HeartRateFailed("No Heart Rate Service Found")
            is ConnectionStatus.Disconnected -> Action.ConnectionFailed("Device Disconnected")
            is ConnectionStatus.Failed -> Action.ConnectionFailed(
                connectionStatus.exception.message ?: "Connection Failed"
            )
            is ConnectionStatus.Authorised -> Action.Authorise
            else -> Action.ConnectionIdle
        }
    }

    private fun mapHeartRate(heartRateStatus: HeartRateStatus?): Action {
        return when (heartRateStatus) {
            HeartRateStatus.Success -> Action.HeartRateSuccess
            is HeartRateStatus.Updated -> Action.HeartRateUpdated(heartRateStatus.heartRate)
            is HeartRateStatus.Failed -> Action.HeartRateFailed(
                heartRateStatus.throwable.message ?: "ERROR"
            )
            else -> Action.HeartRateIdle
        }
    }

    fun connect(macAddress: String) {
        intentDispatcher.connectToMacAddress(macAddress)
    }

    fun checkHeartRate() {
        intentDispatcher.checkHeartRate()
    }

    fun requestAuthorisation() {
        intentDispatcher.authorise()
    }
}

sealed class Action {
    object ConnectionIdle : Action()
    object ConnectionStarted : Action()
    data class ConnectionFailed(val message: String) : Action()
    data class ConnectionDisconnected(val mac: String?) : Action()
    data class ConnectionConnected(val mac: String?) : Action()
    object Authorise : Action()
    object HeartRateNotified : Action()
    object HeartRateIdle : Action()
    object HeartRateSuccess : Action()
    data class HeartRateFailed(val message: String) : Action()
    data class HeartRateUpdated(val heartRate: Int) : Action()
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
