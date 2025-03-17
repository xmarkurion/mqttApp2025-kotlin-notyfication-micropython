package com.markurion.mqtt_home_connect

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class MQTTnotyficationChannel private constructor() {

    private var context: Context? = null
    companion object {
        const val CHANNEL_ID = "MQTTServiceChannel"
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: MQTTnotyficationChannel? = null

        fun getInstance(): MQTTnotyficationChannel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MQTTnotyficationChannel().also { INSTANCE = it }
            }
        }

        fun createChannel(){
            INSTANCE?.createNotificationChannel()
        }

        fun setContext(context: Context) {
            INSTANCE?.context = context
        }
    }

    fun createNotification(message: String): Notification? {
        return this.context?.let {
            // Create an intent for the "Disconnect" action
            val disconnectIntent = Intent(it, DisconnectReceiver::class.java)
            val pendingDisconnectIntent = PendingIntent.getBroadcast(it, 0, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            NotificationCompat.Builder(it, CHANNEL_ID)
                .setContentTitle("MQTT Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.mqtt_logo) // Ensure this icon exists
                .setOngoing(true) // Make the notification non-dismissible
                .addAction(R.drawable.ic_launcher_foreground, "Disconnect", pendingDisconnectIntent) // Add the "Disconnect" button
                .build()
        }
    }

    public fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "MQTT Notifications", // Name of the channel
            NotificationManager.IMPORTANCE_HIGH // Importance level
        ).apply {
            description = "Notifications for the MQTT foreground service" // Description
        }

        val manager = context?.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}