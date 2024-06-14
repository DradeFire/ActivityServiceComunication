package ru.dradefire.activityserviceconnection

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.widget.Toast

class MainService : Service() {
    private var mNM: NotificationManager? = null
    private val mMessenger = Messenger(IncomingHandler())

    @SuppressLint("PrivateResource")
    private fun showNotification() {
        val text = local_service_started

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidx.core.R.drawable.notification_bg_normal)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(local_service_label)
            .setContentText(text)
            .build()

        mNM?.notify(CHANNEL_ID, NOTIFICATION, notification)
    }

    override fun onBind(intent: Intent): IBinder = mMessenger.binder

    override fun onCreate() {
        mNM = getSystemService(NotificationManager::class.java)
        showNotification()
    }

    override fun onDestroy() {
        mNM?.cancel(CHANNEL_ID, NOTIFICATION)
        Toast.makeText(this, remote_service_stopped, Toast.LENGTH_SHORT).show()
    }

    class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> mClients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> mClients.remove(msg.replyTo)
                MSG_SET_VALUE -> {
                    mValue += 1
                    mClients.forEachIndexed { _, messenger ->
                        runCatching {
                            messenger.send(Message.obtain(null, MSG_SET_VALUE, mValue, 0))
                        }.onFailure {
                            mClients.remove(messenger)
                        }
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    companion object {
        private val mClients = ArrayList<Messenger>()
        private var mValue = 0

        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3

        const val remote_service_stopped = "Remote service stopped"
        const val local_service_label = "Local service label"
        const val local_service_started = "Local service started"
        const val remote_service_connected = "Remote service connected"
        const val remote_service_disconnected = "Remote service disconnected"

        const val channel_name = "channel_name"
        const val channel_description = "channel_description"
        const val CHANNEL_ID = "102"

        const val NOTIFICATION = 101
    }
}
