package com.michaelfotiadis.heartbeat.service

import android.app.Notification
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.michaelfotiadis.heartbeat.R
import com.michaelfotiadis.heartbeat.ui.main.MainActivity
import javax.inject.Inject

class ServiceNotificationFactory @Inject constructor() {

    fun getServiceStartedNotification(context: Context): Notification {
        return getDefaultBuilder(context)
            .setContentText(context.getString(R.string.service_started))
            .build()
    }

    fun getEnableBluetoothNotification(context: Context): Notification {
        return getDefaultBuilder(context)
            .setContentText(context.getString(R.string.service_bluetooth_off))
            .setContentIntent(getBluetoothPendingIntent(context))
            .build()
    }

    private fun getDefaultBuilder(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            context.getString(R.string.bluetooth_channel_id)
        )
            .setContentIntent(getMainPendingIntent(context))
            .setSmallIcon(R.drawable.ic_bluetooth_black_24dp)
            .setContentTitle(context.getString(R.string.service_name))
    }

    private fun getMainPendingIntent(context: Context): PendingIntent {
        return MainActivity.createIntent(context).let { notificationIntent ->
            PendingIntent.getActivity(context, 0, notificationIntent, 0)
        }
    }

    private fun getBluetoothPendingIntent(context: Context): PendingIntent {
        return Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).let { notificationIntent ->
                PendingIntent.getActivity(context, 0, notificationIntent, 0)
            }
    }
}
