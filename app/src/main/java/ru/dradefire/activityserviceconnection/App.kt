package ru.dradefire.activityserviceconnection

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import ru.dradefire.activityserviceconnection.MainService.Companion.CHANNEL_ID
import ru.dradefire.activityserviceconnection.MainService.Companion.channel_description
import ru.dradefire.activityserviceconnection.MainService.Companion.channel_name

class App : Application() {

    override fun onCreate() {
        val name = channel_name
        val descriptionText = channel_description
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(mChannel)

        super.onCreate()
    }
}