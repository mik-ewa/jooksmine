package com.example.fitness_tracking_app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.fitness_tracking_app.utils.GlobalStrings
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class JooksmineApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) { Timber.plant(Timber.DebugTree()) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GlobalStrings.RUNNING_CHANNEL,
                GlobalStrings.RUNNING_NOTIFICATIONS,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        Firebase.database.setPersistenceEnabled(true)
    }
}
