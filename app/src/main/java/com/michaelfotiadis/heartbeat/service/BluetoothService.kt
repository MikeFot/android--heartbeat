package com.michaelfotiadis.heartbeat.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.bluetooth.BluetoothHandler
import com.michaelfotiadis.heartbeat.bluetooth.model.HeartRateStatus
import com.michaelfotiadis.heartbeat.repo.BluetoothRepo
import com.michaelfotiadis.heartbeat.repo.MessageRepo
import dagger.android.AndroidInjection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class BluetoothService : LifecycleService() {

    companion object {
        private const val NOTIFICATION_ID = R.integer.bluetooth_service_id
    }

    @Inject
    lateinit var repo: BluetoothRepo
    @Inject
    lateinit var notificationFactory: ServiceNotificationFactory
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var bluetoothHandler: BluetoothHandler
    @Inject
    lateinit var messageRepo: MessageRepo

    private var isStarted = false
    // Binder given to clients
    private val binder = LocalBinder()
    private val scope = CoroutineScope(Job())

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        updateNotification(
            notificationFactory.getServiceStartedNotification()
        )

        repo.actionLiveData.observe(this, Observer(this@BluetoothService::processRepoAction))

        repo.bluetoothConnectionLiveData.observe(
            this,
            Observer(this@BluetoothService::onBluetoothStateUpdated)
        )

        repo.deviceInfoLiveData.observe(this, Observer { deviceInfo ->
            messageRepo.log("Device Info $deviceInfo")
        })
    }

    private fun onBluetoothStateUpdated(isEnabled: Boolean) {
        if (isEnabled) {
            updateNotification(
                notificationFactory.getServiceStartedNotification()
            )
        } else {
            updateNotification(
                notificationFactory.getEnableBluetoothNotification()
            )
        }
    }

    private fun updateHeartRateNotification(heartRateStatus: HeartRateStatus?) {
        if (heartRateStatus is HeartRateStatus.Updated) {
            updateNotification(
                notificationFactory.getHeartRateNotification(
                    heartRateStatus.heartRate
                )
            )
        }
    }

    override fun onDestroy() {
        disconnectDevice()
        bluetoothHandler.cleanup()
        try {
            scope.cancel()
        } catch (exception: CancellationException) {
            messageRepo.logError(exception.message ?: "ERROR")
        }
        super.onDestroy()
    }

    private fun disconnectDevice() {
        bluetoothHandler.cancelScan()
        bluetoothHandler.stopPingHeartRate()
        bluetoothHandler.disconnect()
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
            BluetoothActions.ACTION_REFRESH_BONDED_DEVICES -> refreshBondedDevices()
            BluetoothActions.ACTION_ENABLE_BLUETOOTH -> enableBluetooth()
            BluetoothActions.ACTION_CONNECT_TO_MAC -> connectToMac(
                intent.getStringExtra(
                    BluetoothActions.EXTRA_MAC_ADDRESS
                ) ?: ""
            )
            BluetoothActions.ACTION_SCAN_DEVICES -> scanForDevices()
            BluetoothActions.ACTION_DISCONNECT_DEVICE -> disconnectDevice()
            BluetoothActions.ACTION_CHECK_CONNECTION -> checkConnection()
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
                notificationFactory.getServiceStartedNotification()
            )
            isStarted = true
        }
    }

    private fun processRepoAction(action: BluetoothRepo.Action?) {
        when (action) {
            BluetoothRepo.Action.Idle -> {
                // NOOP
            }
            is BluetoothRepo.Action.Connected -> {
                updateNotification(
                    notificationFactory.getConnectedToDevice(action.name)
                )
                bluetoothHandler.discoverServices()
            }
            is BluetoothRepo.Action.Disconnected -> {
                updateNotification(
                    notificationFactory.getDisconnectedFromDevice(action.name)
                )
                bluetoothHandler.stopPingHeartRate()
            }
            BluetoothRepo.Action.ConnectionFailed -> messageRepo.logError("Connection Failed")
            BluetoothRepo.Action.ServicesDiscovered -> notifyAuth()
            BluetoothRepo.Action.AuthorisationNotified -> bluetoothHandler.executeAuthorisationSequence()
            BluetoothRepo.Action.AuthorisationComplete -> updateDeviceInfo()
            BluetoothRepo.Action.AuthorisationStepOne -> messageRepo.log("Auth Step One")
            BluetoothRepo.Action.AuthorisationStepTwo -> messageRepo.log("Auth Step Two")
            BluetoothRepo.Action.AuthorisationFailed -> {
                // NOOP
            }
        }
    }

    private fun notifyAuth() {
        scope.launch {
            bluetoothHandler.notifyAuthorisation()
        }
    }

    private fun connectToMac(macAddress: String) {
        scope.launch {
            bluetoothHandler.connectToMacInitial(macAddress)
        }
    }

    private fun checkConnection() {
        scope.launch {
            bluetoothHandler.checkConnection()
        }
    }

    private fun enableBluetooth() {
        scope.launch {
            bluetoothHandler.askToEnableBluetooth(applicationContext)
        }
    }

    private fun refreshBondedDevices() {
        scope.launch {
            bluetoothHandler.getBondedDevices()
        }
    }

    private fun scanForDevices() {
        scope.launch {
            bluetoothHandler.scan(repo.scanStatusLiveData::postValue)
        }
    }

    private fun updateDeviceInfo() {
        scope.launch {
            delay(1_000)
            bluetoothHandler.refreshDeviceInfo()
        }
    }
}
