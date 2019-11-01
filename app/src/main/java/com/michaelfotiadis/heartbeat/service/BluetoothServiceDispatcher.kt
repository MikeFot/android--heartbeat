package com.michaelfotiadis.heartbeat.service

import android.content.Context
import android.content.Intent
import android.os.Build

class BluetoothServiceDispatcher(
    private val context: Context
) {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, BluetoothService::class.java)
        }
    }

    fun createIntent(): Intent {
        return getDefaultIntent()
    }

    fun startService() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothService.ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun stopService() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothService.ACTION_STOP
            context.startService(intent)
        }
    }

    fun checkBluetoothConnection() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothService.ACTION_CHECK_CONNECTION
            context.startService(intent)
        }
    }

    fun refreshBondedDevices() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothService.ACTION_REFRESH_BONDED_DEVICES
            context.startService(intent)
        }
    }

    fun askToEnableBluetooth() {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothService.ACTION_ENABLE_BLUETOOTH
            context.startService(intent)
        }
    }

    fun connectToMacAddress(mac: String) {
        getDefaultIntent().also { intent ->
            intent.action = BluetoothService.ACTION_CONNECT_TO_MAC
            intent.putExtra(BluetoothService.EXTRA_MAC_ADDRESS, mac)
            context.startService(intent)
        }
    }

    private fun getDefaultIntent() = Intent(context, BluetoothService::class.java)
}