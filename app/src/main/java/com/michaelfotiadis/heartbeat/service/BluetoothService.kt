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
import androidx.lifecycle.Observer
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.OtherException
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothStatusProvider
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothWrapper
import com.michaelfotiadis.heartbeat.bluetooth.model.ConnectionStatus
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.core.logger.AppLogger
import dagger.android.AndroidInjection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import javax.inject.Inject

class BluetoothService : LifecycleService() {

    companion object {
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
    private var connectedDevice: BleDevice? = null
    private var tickerChannel: ReceiveChannel<Unit>? = null

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

        bluetoothStatusProvider.connectedDevice.observe(
            this,
            Observer(this::updateConnectedDevice)
        )
        bluetoothStatusProvider.heartRateStatus.observe(
            this,
            Observer(this::updateHeartRateNotification)
        )
    }

    private fun updateHeartRateNotification(heartRateStatus: HeartRateStatus?) {
        if (heartRateStatus is HeartRateStatus.Updated) {
            updateNotification(
                notificationFactory.getHeartRateNotification(
                    this,
                    heartRateStatus.heartRate
                )
            )
        }
    }

    private fun updateConnectedDevice(bleDevice: BleDevice?) {
        if (bleDevice != null) {
            updateNotification(
                notificationFactory.getConnectedToDevice(
                    this,
                    bleDevice.name ?: ""
                )
            )
        } else {
            stopTicker()
            if (connectedDevice != null) {
                updateNotification(
                    notificationFactory.getDisconnectedFromDevice(
                        this,
                        connectedDevice!!.name ?: ""
                    )
                )
            }
        }
        connectedDevice = bleDevice
    }

    override fun onDestroy() {
        disconnectDevice()
        unregisterReceiver(receiver)
        try {
            scope.cancel()
        } catch (exception: CancellationException) {
            appLogger.get().e(exception)
        }
        super.onDestroy()
    }

    private fun disconnectDevice() {
        bluetoothWrapper.cancelScan()
        stopTicker()
        connectedDevice?.let { bleDevice ->
            appLogger.get().d("Disconnected Device")
            bluetoothWrapper.stopHeartRate(bleDevice) {
                bluetoothWrapper.stopNotifyHeartService(bleDevice)
                bluetoothWrapper.disconnect(bleDevice)
                bluetoothStatusProvider.connectionStatusLiveData.postValue(null)
            }
        }
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
            BluetoothActions.ACTION_START -> handleActionStart()
            BluetoothActions.ACTION_STOP -> handleActionStop()
            BluetoothActions.ACTION_CHECK_CONNECTION -> checkConnection()
            BluetoothActions.ACTION_REFRESH_BONDED_DEVICES -> refreshBondedDevices()
            BluetoothActions.ACTION_ENABLE_BLUETOOTH -> enableBluetooth()
            BluetoothActions.ACTION_CONNECT_TO_MAC -> connectToMac(
                intent.getStringExtra(
                    BluetoothActions.EXTRA_MAC_ADDRESS
                ) ?: ""
            )
            BluetoothActions.ACTION_CHECK_HEART_SERVICE -> monitorHeartRate()
            BluetoothActions.ACTION_SCAN_DEVICES -> scanForDevices()
            BluetoothActions.ACTION_DISCONNECT_DEVICE -> disconnectDevice()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNotification(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
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
            try {
                bluetoothWrapper.connectToMac(
                    mac
                ) { bluetoothStatusProvider.connectionStatusLiveData.postValue(it) }
            } catch (exception: IllegalArgumentException) {
                bluetoothStatusProvider.connectionStatusLiveData.postValue(
                    ConnectionStatus.Failed(
                        bleDevice = null,
                        exception = OtherException(exception.message)
                    )
                )
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

    private fun checkDeviceHeartService() {
        scope.launch {
            val device = connectedDevice
            if (device != null) {
                bluetoothWrapper.askForSingleHeartRate(device) { heartRateStatus ->
                    when (heartRateStatus) {
                        is HeartRateStatus.Updated, is HeartRateStatus.Failed -> bluetoothWrapper.stopNotifyHeartService(
                            device
                        )
                        else -> {
                            // NOOP
                        }
                    }
                    bluetoothStatusProvider.heartRateStatus.postValue(heartRateStatus)
                }
            } else {
                appLogger.get().e("Null connected device")
            }
        }
    }

    private fun monitorHeartRate() {
        scope.launch {
            val device = connectedDevice
            if (device != null) {
                bluetoothWrapper.askForContinuousHeartRate(device) { heartRateStatus ->
                    @Suppress("EXPERIMENTAL_API_USAGE")
                    when (heartRateStatus) {
                        HeartRateStatus.Success -> startTicker(device)
                        is HeartRateStatus.Failed -> stopTicker()
                        else -> {
                            // NOOP
                        }
                    }
                    bluetoothStatusProvider.heartRateStatus.postValue(heartRateStatus)
                }

            }
        }
    }

    @ObsoleteCoroutinesApi
    private fun startTicker(device: BleDevice) {
        scope.launch {
            stopTicker()
            tickerChannel = ticker(delayMillis = 10_000, initialDelayMillis = 10_000)
            for (event in tickerChannel!!) {
                bluetoothWrapper.pingHeartRateControl(device)
            }
        }
    }

    private fun stopTicker() {
        tickerChannel?.cancel()
        tickerChannel = null
    }

    private fun scanForDevices() {
        scope.launch {
            bluetoothWrapper.scan(bluetoothStatusProvider.scanStatusLiveData::postValue)
        }
    }
}
