package com.michaelfotiadis.heartbeat.service

import android.content.Context
import android.content.Intent
import android.os.Build

class BluetoothServiceDispatcher(context: Context) {

    private val applicationContext = context.applicationContext

    fun createIntent(): Intent {
        return getDefaultIntent()
    }

    fun startService() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
        }
    }

    fun stopService() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_STOP))
    }

    fun refreshBondedDevices() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_REFRESH_BONDED_DEVICES))
    }

    fun checkConnection() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_CHECK_CONNECTION))
    }

    fun askToEnableBluetooth() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_ENABLE_BLUETOOTH))
    }

    fun connectToMacAddress(mac: String) {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_CONNECT_TO_MAC
            intent.putExtra(BluetoothActions.EXTRA_MAC_ADDRESS, mac)
            applicationContext.startService(intent)
        }
    }

    fun authorise() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_AUTHORISE))
    }

    fun checkHeartRate() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_CHECK_HEART_SERVICE))
    }

    fun scanForDevices() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_SCAN_DEVICES))
    }

    fun disconnectDevice() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_DISCONNECT_DEVICE))
    }

    fun refreshDeviceInfo() {
        applicationContext.startService(getIntentWithAction(BluetoothActions.ACTION_REFRESH_DEVICE_INFO))
    }

    private fun getDefaultIntent() = Intent(applicationContext, BluetoothService::class.java)

    private fun getIntentWithAction(action: String) = getDefaultIntent().apply {
        this.action = action
    }
}
