package com.markurion.mqtt_home_connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var btnSend:Button
    private final var TAG = "MainActivity"

    // On incoming MQTT messages, this BroadcastReceiver will be triggered
    private val mqttMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val topic = intent.getStringExtra("topic")
            val message = intent.getStringExtra("message")

            Log.d("MainActivity", "Message received: $topic -> $message")

            if (message == "TriggerAction") {
                publishMessage("Action triggered response!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        //Set UI
//        btnSend = findViewById(R.id.btn_send)
//        btnSend.setOnClickListener {
//            Log.d(TAG, "onCreate: Should publish Message")
//            publishMessage("TriggerAction")
//        }

        // Check if the app has notification access & if not, ask the user to open settings
        NotificationPermissionCheck.checkNotificationAccess(this)

        // Start the MQTT background service
        val serviceIntent = Intent(this, MQTTService::class.java)
        startService(serviceIntent)

        // Register the BroadcastReceiver
        val intentFilter = IntentFilter("MQTTMessageReceived")
        registerReceiver(mqttMessageReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    }

    private fun publishMessage(content: String) {
        val serviceIntent = Intent(this, MQTTService::class.java)
        serviceIntent.putExtra("action", "publish")
        serviceIntent.putExtra("topic", "a00300334/phone")
        serviceIntent.putExtra("message", content)
        startService(serviceIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mqttMessageReceiver)
        val serviceIntent = Intent(this, MQTTService::class.java)
        stopService(serviceIntent)
    }

    override fun onResume() {
        super.onResume()
//        checkNotificationAccess(this)
    }
}

