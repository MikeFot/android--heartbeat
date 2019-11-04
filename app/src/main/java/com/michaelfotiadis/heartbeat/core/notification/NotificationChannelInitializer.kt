package com.michaelfotiadis.heartbeat.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Resources
import android.os.Build
import com.michaelfotiadis.heartbeat.R
import javax.inject.Inject

class NotificationChannelInitializer @Inject constructor(
    private val resources: Resources,
    private val notificationManager: NotificationManager
) {

    fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = resources.getString(R.string.bluetooth_channel_id)
            val channelName = resources.getString(R.string.bluetooth_channel_name)
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).also { channel ->
                channel.description = resources.getString(R.string.bluetooth_channel_description)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
