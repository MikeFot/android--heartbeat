package com.michaelfotiadis.heartbeat.repo

import androidx.lifecycle.MutableLiveData
import com.chibatching.kotpref.KotprefModel
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceInfo
import com.michaelfotiadis.heartbeat.bluetooth.model.DeviceResult
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BluetoothRepo(private val executionThreads: ExecutionThreads) : KotprefModel() {

    sealed class Action {
        object Idle : Action()
        object Connecting : Action()
        data class Connected(val name: String) : Action()
        data class Disconnected(val name: String) : Action()
        object ConnectionFailed : Action()
        object ServicesDiscovered : Action()
        object AuthorisationNotified : Action()
        object AuthorisationStepOne : Action()
        object AuthorisationStepTwo : Action()
        object AuthorisationFailed : Action()
        object AuthorisationComplete : Action()
    }

    val actionLiveData = MutableLiveData<Action>()

    private var lastMessageTimestamp = 0L
    private val messageDelayMs = TimeUnit.MILLISECONDS.toMillis(200L)

    fun postAction(action: Action) {
        executionThreads.actionScope.launch {
            if (System.currentTimeMillis() - lastMessageTimestamp < messageDelayMs) {
                delay(messageDelayMs)
            }
            lastMessageTimestamp = System.currentTimeMillis()
            actionLiveData.postValue(action)
        }
    }

    val bondedDevicesLiveData = MutableLiveData<Set<DeviceResult>>()
        .apply { postValue(setOf()) }
    val scanStatusLiveData = MutableLiveData<ScanStatus>()
    val deviceInfoLiveData = MutableLiveData<DeviceInfo>()

    var connectedMacAddress by nullableStringPref(default = null)

    val bluetoothConnectionLiveData = BluetoothConnectionLiveData(context)

}