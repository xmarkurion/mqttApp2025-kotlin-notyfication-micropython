package com.markurion.mqtt_home_connect
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MQTTService : Service() {
    private lateinit var mqttClient: MqttClient
    private val brokerUrl = Finals.brokerUrl
    private val clientId = Finals.clientId
    private var notificationId = 2 // Start from 2 to avoid conflict with the foreground notification

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupNotyfication()
        setupMqtt()

        intent?.let {
            if (it.getStringExtra("action") == "publish") {
                val topic = it.getStringExtra("topic")
                val message = it.getStringExtra("message")
                publishMessage(topic, message)
            }
        }
        return START_STICKY
    }

//    private fun setupNotyfication(){
//        val notificationChannel = MQTTnotyficationChannel.getInstance()
//        MQTTnotyficationChannel.setContext(this)
//        val msg: Notification? = notificationChannel.createNotification("MQTT Service is running...")
//        startForeground(1, msg)
//    }

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

            val options = MqttConnectOptions().apply {
                isCleanSession = true
                connectionTimeout = 10
                keepAliveInterval = 20
                mqttVersion = MqttConnectOptions.MQTT_VERSION_DEFAULT
            }

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e("MQTTService", "Connection lost: ${cause?.message}")
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
                    val msg: Notification? = notificationChannel.createNotification("New message: ${String(message.payload)}")
                    if (msg != null) {
                        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(notificationId, msg)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken) {
                    Log.d("MQTTService", "Delivery complete")
                }
            })

            mqttClient.connect(options)
            mqttClient.subscribe("a00300334/#", 0)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun publishMessage(topic: String?, message: String?) {
        if (topic != null && message != null) {
            try {
                val mqttMessage = MqttMessage()
                mqttMessage.payload = message.toByteArray()
                mqttClient.publish(topic, mqttMessage)
                Log.d("MQTTService", "Message published: $topic -> $message")
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
        return null
    }
}