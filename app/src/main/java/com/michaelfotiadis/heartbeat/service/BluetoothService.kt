package com.michaelfotiadis.heartbeat.service

import android.app.Notification
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothWrapper
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import javax.inject.Inject

class BluetoothService : LifecycleService() {

    companion object {
        const val ACTION_START = "action.bluetooth_start"
        const val ACTION_STOP = "action.bluetooth_stop"
        const val ACTION_CHECK_CONNECTION = "action.check_connection"
        const val ACTION_REFRESH_BONDED_DEVICES = "action.refresh_bonded_devices"
        const val ACTION_ENABLE_BLUETOOTH = "action.enable_bluetooth"
        const val ACTION_CONNECT_TO_MAC = "action.connect_to_mac"
        const val EXTRA_MAC_ADDRESS = "extra.mac_address"
        private const val NOTIFICATION_ID = R.integer.bluetooth_service_id
    }

    @Inject
    lateinit var bluetoothStatusProvider: BluetoothStatusProvider
    @Inject
    lateinit var notificationFactory: ServiceNotificationFactory
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var bluetoothWrapper: BluetoothWrapper
    @Inject
    lateinit var appLogger: AppLogger

    private var isStarted = false
    // Binder given to clients
    private val binder = LocalBinder()
    private val scope = CoroutineScope(Job())

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        bluetoothStatusProvider.isConnectedLiveData.postValue(false)
                        updateNotification(
                            notificationFactory.getEnableBluetoothNotification(applicationContext)
                        )
                    }
                    BluetoothAdapter.STATE_ON -> {
                        bluetoothStatusProvider.isConnectedLiveData.postValue(true)
                        updateNotification(
                            notificationFactory.getServiceStartedNotification(applicationContext)
                        )
                    }
                }
            }
        }
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onDestroy() {
        bluetoothWrapper.cancelScan()
        // bluetoothWrapper.disconnect()
        unregisterReceiver(receiver)
        try {
            scope.cancel()
        } catch (exception: CancellationException) {
            appLogger.get().e(exception)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of BluetoothService so clients can call public methods
        fun getService(): BluetoothService = this@BluetoothService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START -> handleActionStart()
            ACTION_STOP -> handleActionStop()
            ACTION_CHECK_CONNECTION -> checkConnection()
            ACTION_REFRESH_BONDED_DEVICES -> refreshBondedDevices()
            ACTION_ENABLE_BLUETOOTH -> enableBluetooth()
            ACTION_CONNECT_TO_MAC -> connectToMac(intent.getStringExtra(EXTRA_MAC_ADDRESS) ?: "")
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleActionStop() {
        if (isStarted) {
            stopForeground(true)
            stopSelf()
        }
    }

    private fun handleActionStart() {
        if (!isStarted) {
            startForeground(
                NOTIFICATION_ID,
                notificationFactory.getServiceStartedNotification(this)
            )
            isStarted = true
        }
    }

    private fun connectToMac(mac: String) {
        scope.launch {
            bluetoothWrapper.connectToMac(mac) { connectionResult ->
                appLogger.get("SERVICE").d("Connection result $connectionResult")
            }
        }
    }

    private fun checkConnection() {
        scope.launch {
            val isEnabled = bluetoothWrapper.isBluetoothEnabled()
            bluetoothStatusProvider.isConnectedLiveData.postValue(isEnabled)
        }
    }

    private fun enableBluetooth() {
        scope.launch {
            bluetoothWrapper.askToEnableBluetooth(applicationContext)
        }
    }

    private fun refreshBondedDevices() {
        scope.launch {
            val bondedDevices = bluetoothWrapper.getBondedDevices()
            bluetoothStatusProvider.bondedDevicesLiveData.postValue(bondedDevices)
        }
    }

    private fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

