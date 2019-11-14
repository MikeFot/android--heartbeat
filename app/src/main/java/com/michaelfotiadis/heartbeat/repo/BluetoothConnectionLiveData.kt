package com.michaelfotiadis.heartbeat.repo

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData

class BluetoothConnectionLiveData(private val context: Context) : MutableLiveData<Boolean>() {

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