package com.michaelfotiadis.heartbeat.repo

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chibatching.kotpref.KotprefModel
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.ScanStatus
import com.michaelfotiadis.heartbeat.core.scheduler.ExecutionThreads
import com.polidea.rxandroidble2.RxBleClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BluetoothRepo(private val executionThreads: ExecutionThreads) : KotprefModel() {

    sealed class Action {
        object Idle : Action()
        object Connecting : Action()
        data class Connected(val name: String) : Action()
        data class Disconnected(val name: String) : Action()
        object ServicesDiscovered : Action()
        object AuthorisationNotified : Action()
        object AuthorisationStepOne : Action()
        object AuthorisationStepTwo : Action()
        object AuthorisationFailed : Action()
        object AuthorisationComplete : Action()
    }

    val actionLiveData = MutableLiveData<Action>()

    private var lastMessageTimestamp = 0L
    private val messageDelayMs = TimeUnit.MILLISECONDS.toMillis(100L)

    fun postAction(action: Action) {
        executionThreads.actionScope.launch {
            if (System.currentTimeMillis() - lastMessageTimestamp < messageDelayMs) {
                delay(messageDelayMs)
            }
            actionLiveData.postValue(action)
        }
    }


    val bluetoothStateLiveData = MutableLiveData<RxBleClient.State>()
    val bondedDevicesLiveData = MutableLiveData<Set<BluetoothDevice>>()
    val scanStatusLiveData = MutableLiveData<ScanStatus>()
    val connectionStatusLiveData = MutableLiveData<ConnectionStatus>()
    val heartRateStatus = MutableLiveData<HeartRateStatus>()
    val heartRateExists: LiveData<Boolean> =
        Transformations.map(heartRateStatus) { heartRateStatus ->
            when (heartRateStatus) {
                is HeartRateStatus.Success, is HeartRateStatus.Updated -> true
                is HeartRateStatus.Failed -> false
            }
        }


    val servicesDiscovered = MutableLiveData<Boolean>().apply { postValue(false) }
    val authorisationAttempted = MutableLiveData<Boolean>().apply { postValue(false) }
    var connectedMacAddress by nullableStringPref(default = null)


}
