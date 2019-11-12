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
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
    lateinit var bluetoothWrapper: BluetoothHandler
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
            notificationFactory.getServiceStartedNotification(applicationContext)
        )

        repo.actionLiveData.observe(this, Observer(this@BluetoothService::processRepoAction))

        repo.bluetoothConnectionLiveData.observe(
            this,
            Observer(this@BluetoothService::onBluetoothStateUpdated)
        )
    }

    private fun onBluetoothStateUpdated(isEnabled: Boolean) {
        if (isEnabled) {
            updateNotification(
                notificationFactory.getServiceStartedNotification(applicationContext)
            )
        } else {
            updateNotification(
                notificationFactory.getEnableBluetoothNotification(applicationContext)
            )
        }
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

    override fun onDestroy() {
        disconnectDevice()
        bluetoothWrapper.cleanup()
        try {
            scope.cancel()
        } catch (exception: CancellationException) {
            messageRepo.logError(exception.message ?: "ERROR")
        }
        super.onDestroy()
    }

    private fun disconnectDevice() {
        bluetoothWrapper.cancelScan()
        bluetoothWrapper.stopPingHeartRate()
        bluetoothWrapper.disconnect()
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
                notificationFactory.getServiceStartedNotification(this)
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
                    notificationFactory.getConnectedToDevice(
                        applicationContext,
                        action.name
                    )
                )
                bluetoothWrapper.discoverServices()
            }
            is BluetoothRepo.Action.Disconnected -> {
                updateNotification(
                    notificationFactory.getDisconnectedFromDevice(
                        applicationContext,
                        action.name
                    )
                )
                bluetoothWrapper.stopPingHeartRate()
            }
            BluetoothRepo.Action.ConnectionFailed -> messageRepo.logError("Connection Failed")
            BluetoothRepo.Action.ServicesDiscovered -> bluetoothWrapper.notifyAuthorisation()
            BluetoothRepo.Action.AuthorisationNotified -> bluetoothWrapper.executeAuthorisationSequence()
            BluetoothRepo.Action.AuthorisationComplete -> {
                Toasty.info(
                    applicationContext,
                    "Done"
                ).show()
                bluetoothWrapper.startPingHeartRate()
            }
            BluetoothRepo.Action.AuthorisationStepOne -> messageRepo.log("Auth Step One")
            BluetoothRepo.Action.AuthorisationStepTwo -> messageRepo.log("Auth Step Two")
            BluetoothRepo.Action.AuthorisationFailed -> Toasty.error(
                applicationContext,
                "Auth Failed"
            ).show()
        }
    }

    private fun connectToMac(macAddress: String) {
        scope.launch {
            bluetoothWrapper.connectToMacInitial(macAddress)
        }
    }

    private fun checkConnection() {
        scope.launch {
            bluetoothWrapper.checkConnection()
        }
    }

    private fun enableBluetooth() {
        scope.launch {
            bluetoothWrapper.askToEnableBluetooth(applicationContext)
        }
    }

    private fun refreshBondedDevices() {
        scope.launch {
            bluetoothWrapper.getBondedDevices()
        }
    }

    private fun scanForDevices() {
        scope.launch {
            bluetoothWrapper.scan(repo.scanStatusLiveData::postValue)
        }
    }
}
