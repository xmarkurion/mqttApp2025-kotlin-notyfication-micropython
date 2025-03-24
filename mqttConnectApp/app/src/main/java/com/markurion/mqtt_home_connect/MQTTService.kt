package com.markurion.mqtt_home_connect
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.packet.MqttProperties

class MQTTService : Service() {
    private val TAG = "MQTTService"
    private lateinit var mqttClient: MqttClient
    private val brokerUrl = Finals.brokerUrl
    private val clientId = Finals.clientId
    private var notificationId = 3 // Start from 3 to avoid conflict with the foreground notification

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MQTTService = this@MQTTService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotyfication()
        setupMqtt()

        intent?.let {
            if (it.getStringExtra("action") == "publish") {
                val topic = it.getStringExtra("topic")
                val message = it.getStringExtra("message")
                Log.d(TAG, "onStartCommand: $topic -> $message")
                publishMessage(topic, message)
            }
        }
        return START_STICKY
    }

    private fun setupNotyfication() {
        val notificationChannel = MQTTnotyficationChannel.getInstance()
        MQTTnotyficationChannel.setContext(this)
        MQTTnotyficationChannel.createChannel()

        val msg: Notification? = notificationChannel.createNotification("MQTT Service is running...")
        if (msg != null) {
            this.startForeground(1, msg)
        } else {
            Log.e("MQTTService", "Failed to create notification")
        }
    }

    private fun setupMqtt() {
        try {
            mqttClient = MqttClient(brokerUrl, clientId, null)

            val options = MqttConnectionOptions().apply {
                isCleanStart = true
                connectionTimeout = 10
                keepAliveInterval = 20
            }

            mqttClient.setCallback(object : MqttCallback {
                override fun disconnected(disconnectResponse: MqttDisconnectResponse?) {
                    Log.e("MQTTService", "Connection lost: ${disconnectResponse.toString()}")
                }

                override fun mqttErrorOccurred(exception: MqttException?) {
                    TODO("Not yet implemented")
                }

                @SuppressLint("ServiceCast")
                override fun messageArrived(topic: String, message: MqttMessage) {
                    Log.d("MQTTService", "Message arrived: $topic -> ${String(message.payload)}")

                    // Broadcast the message to MainActivity
                    val broadcastIntent =Intent("MQTTMessageReceived")
                    broadcastIntent.putExtra("topic", topic)
                    broadcastIntent.putExtra("message", String(message.payload))
                    sendBroadcast(broadcastIntent)

                    // Create a new dismissible notification for each message
                    val notificationChannel = MQTTnotyficationChannel.getInstance()
                    val msg: Notification? = notificationChannel.createShortNotification(topic,"New message: ${String(message.payload)}")
                    val summary: Notification? = notificationChannel.createGroupSummaryNotification()
                    if (msg != null) {
                        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(notificationId++, msg)
                        notificationManager.notify(2, summary)
                    }
                }

                override fun deliveryComplete(token: IMqttToken?) {
                    Log.d("MQTTService", "Delivery complete")
                }

                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Log.d("MQTTService", "Connect complete")
                }

                override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) {
                    Log.d("MQTTService", "Auth packet arrived")
                }
            })

            mqttClient.connect(options)
            mqttClient.subscribe("a00300334/led/#", 0)
            mqttClient.subscribe("a00300334/pir", 0)
            mqttClient.subscribe("a00300334/light", 0)
            mqttClient.subscribe("a00300334/action", 0)

        } catch (e: Exception) {
            Log.d(TAG, "setupMqtt: " + e.toString())
            e.printStackTrace()
            e.printStackTrace()
        }
    }

    fun publishMessage(topic: String?, message: String?) {
        if (topic != null && message != null) {
            try {
                val mqttMessage = MqttMessage()
                mqttMessage.payload = message.toByteArray()
                Log.d("MQTTService", "Message before published: $topic -> $message")
                mqttClient.publish(topic, mqttMessage)
                Log.d(TAG, "publishMessage: Published")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.disconnect()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder;
    }
}