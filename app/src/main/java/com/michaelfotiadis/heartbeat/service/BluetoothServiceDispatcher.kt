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
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_STOP
            applicationContext.startService(intent)
        }
    }

    fun checkBluetoothConnection() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_CHECK_CONNECTION
            applicationContext.startService(intent)
        }
    }

    fun refreshBondedDevices() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_REFRESH_BONDED_DEVICES
            applicationContext.startService(intent)
        }
    }

    fun askToEnableBluetooth() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_ENABLE_BLUETOOTH
            applicationContext.startService(intent)
        }
    }

    fun connectToMacAddress(mac: String) {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_CONNECT_TO_MAC
            intent.putExtra(BluetoothActions.EXTRA_MAC_ADDRESS, mac)
            applicationContext.startService(intent)
        }
    }

    fun checkInfo() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_CHECK_HEART_SERVICE
            applicationContext.startService(intent)
        }
    }

    fun scanForDevices() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_SCAN_DEVICES
            applicationContext.startService(intent)
        }
    }

    fun disconnectDevice() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothActions.ACTION_DISCONNECT_DEVICE
            applicationContext.startService(intent)
        }
    }

    private fun getDefaultIntent() = Intent(applicationContext, BluetoothService::class.java)
}
