package com.michaelfotiadis.heartbeat.repo

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import com.chibatching.kotpref.KotprefModel
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
    val authorisationAttempted = MutableLiveData<Boolean>()
        .apply { postValue(false) }
    var connectedMacAddress by nullableStringPref(default = null)

    val bluetoothConnectionLiveData = object : MutableLiveData<Boolean>() {

        private val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )) {
                        BluetoothAdapter.STATE_OFF -> postValue(false)
                        BluetoothAdapter.STATE_ON -> postValue(true)
                    }
                }
            }
        }

        override fun onActive() {
            super.onActive()
            context.applicationContext.registerReceiver(
                receiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            )
        }

        override fun onInactive() {
            super.onInactive()
            context.applicationContext.unregisterReceiver(receiver)
        }

    }

}
