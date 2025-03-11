package com.example.fitness_tracking_app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    companion object {
        private const val DEFAULT_CHANNEL_ID = "TRACKING_CHANNEL"
        private const val DEFAULT_CHANNEL_NAME = "Tracking Notification"
        private const val DEFAULT_CHANNEL_DESC = "Notifications for tracking"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel(
        channelId: String = DEFAULT_CHANNEL_ID,
        channelName: String = DEFAULT_CHANNEL_NAME,
        channelDescription: String = DEFAULT_CHANNEL_DESC
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        channelId: String = DEFAULT_CHANNEL_ID,
        smallIcon: Int,
        title: String,
        text: String,
        pendingIntent: PendingIntent
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
    }

    fun updateNotification(
        notificationId: Int,
        builder: NotificationCompat.Builder,
        title: String,
        text: String
    ) {
        builder.setContentTitle(title).setContentText(text)
        notificationManager.notify(notificationId, builder.build())
    }
}
