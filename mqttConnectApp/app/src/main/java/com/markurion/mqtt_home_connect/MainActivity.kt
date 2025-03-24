package com.markurion.mqtt_home_connect

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private final var TAG = "MainActivity"

    private var iv_red: ImageView? = null
    private var red: Boolean = false
    private var btn_red: Button? = null

    private var iv_yellow: ImageView? = null
    private var yellow: Boolean = false
    private var btn_yellow: Button? = null

    private var iv_green: ImageView? = null
    private var btn_green: Button? = null
    private var green: Boolean = false

    private var iv_direction: ImageView? = null
    private var tv_light: TextView? = null

    private var mqttService: MQTTService? = null
    private var isServiceBound = false

    // On incoming MQTT messages, this BroadcastReceiver will be triggered
    private val mqttMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val topic = intent.getStringExtra("topic")
            val message = intent.getStringExtra("message")

            Log.d("MainActivity", "Message received: $topic -> $message")

            if (message == "TriggerAction") {
                publishMessage("a00300334/action", "Action triggered response!")
            }

            if (topic != null && topic.startsWith("a00300334/light")) {
                val lightValue = message
                tv_light?.text = "Light reading: $lightValue"
            }

            if (topic != null && topic.startsWith("a00300334/action")) {
                when (message) {
                    "right" -> iv_direction?.setImageResource(R.drawable.baseline_arrow_forward_24)
                    "left" -> iv_direction?.setImageResource(R.drawable.baseline_arrow_back_24)
                    "up" -> iv_direction?.setImageResource(R.drawable.baseline_arrow_upward_24)
                    "down" -> iv_direction?.setImageResource(R.drawable.baseline_arrow_downward_24)
                    "far" -> iv_direction?.setImageResource(R.drawable.baseline_brightness_low_24)
                }
                resetDrawable()
            }

            if (topic != null && topic.startsWith("a00300334/led")) {
                when (topic) {
                    "a00300334/led/red" -> when(message){
                        "1" -> {
                            iv_red?.setColorFilter(Color.RED)
                            red = true
                        }
                        "0" -> {
                            iv_red?.setColorFilter(Color.GRAY)
                            red = false
                        }
                    }
                    "a00300334/led/yellow" -> when(message){
                        "1" -> {
                            iv_yellow?.setColorFilter(Color.YELLOW)
                            yellow = true
                        }
                        "0" -> {
                            iv_yellow?.setColorFilter(Color.GRAY)
                            yellow = false
                        }
                    }
                    "a00300334/led/green" -> when(message){
                        "1" -> {
                            iv_green?.setColorFilter(Color.GREEN)
                            green = true
                        }
                        "0" -> {
                            iv_green?.setColorFilter(Color.GRAY)
                            green = false
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the app has notification access & if not, ask the user to open settings
        NotificationPermissionCheck.checkNotificationAccess(this)

        // Start the MQTT background service if not already running
        if (!isServiceRunning(MQTTService::class.java)) {
            val serviceIntent = Intent(this, MQTTService::class.java)
            startService(serviceIntent)
        }

        // Bind to the MQTTService
        val serviceIntent = Intent(this, MQTTService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Register the BroadcastReceiver
        val filter = IntentFilter("MQTTMessageReceived")
        registerReceiver(mqttMessageReceiver, filter, Context.RECEIVER_EXPORTED)

        // Setup UI
        setupUI()
    }

    private val serviceConnection = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName, service: IBinder) {
            val binder = service as MQTTService.LocalBinder
            mqttService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: android.content.ComponentName) {
            mqttService = null
            isServiceBound = false
        }
    }

    private fun publishMessage(topic: String, content: String) {
        if (isServiceBound) {
            mqttService?.publishMessage(topic, content)
        } else {
            Log.e(TAG, "Service is not bound")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mqttMessageReceiver)
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (!NotificationPermissionCheck.areNotificationsEnabled(this)) {
            NotificationPermissionCheck.checkNotificationAccess(this)
        }
    }

    private fun setupUI() {
        iv_red = findViewById(R.id.iv_red)
        iv_yellow = findViewById(R.id.iv_yellow)
        iv_green = findViewById(R.id.iv_green)
        tv_light = findViewById(R.id.tv_light)
        iv_direction = findViewById(R.id.iv_dir)

        iv_red?.setColorFilter(Color.GRAY)
        iv_yellow?.setColorFilter(Color.GRAY)
        iv_green?.setColorFilter(Color.GRAY)

        btn_red = findViewById(R.id.btn_red)
        btn_red?.setOnClickListener {
            red = !red
            pubMessageToChangeColor("red", if (red) "1" else "0")
        }

        btn_yellow = findViewById(R.id.btn_yellow)
        btn_yellow?.setOnClickListener {
            yellow = !yellow
            pubMessageToChangeColor("yellow", if (yellow) "1" else "0")
        }

        btn_green = findViewById(R.id.btn_green)
        btn_green?.setOnClickListener {
            green = !green
            pubMessageToChangeColor("green", if (green) "1" else "0")
        }

        tv_light?.text = "Light reading: 0"
    }

    fun pubMessageToChangeColor(color: String, value: String){
        publishMessage("a00300334/led/"+color, value)
    }

    /**
     * Reset the direction drawable after 2 seconds
     */
    fun resetDrawable() {
        Handler(Looper.getMainLooper()).postDelayed({
            iv_direction?.setImageResource(R.drawable.empty)
        }, 2000)
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}