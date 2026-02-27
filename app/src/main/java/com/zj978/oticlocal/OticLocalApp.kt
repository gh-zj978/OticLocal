package com.zj978.oticlocal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class OticLocalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "1", "Audio Streaming", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Used to notify about the audio streaming status."
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}