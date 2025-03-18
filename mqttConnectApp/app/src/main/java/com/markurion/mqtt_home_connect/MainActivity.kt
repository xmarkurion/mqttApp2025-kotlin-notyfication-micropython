package com.markurion.mqtt_home_connect

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color

import android.os.Bundle
import android.os.Handler
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


    // On incoming MQTT messages, this BroadcastReceiver will be triggered
    private val mqttMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val topic = intent.getStringExtra("topic")
            val message = intent.getStringExtra("message")

            Log.d("MainActivity", "Message received: $topic -> $message")

            if (message == "TriggerAction") {
                publishMessage("a00300334/action","Action triggered response!")
            }

            if (topic != null && topic.startsWith("a00300334/light")) {
                val lightValue = message
                tv_light?.text = "Light reading: $lightValue"
            }

            if(topic != null && topic.startsWith("a00300334/action")){
                if (message == "right"){
                    iv_direction?.setImageResource(R.drawable.baseline_arrow_forward_24)
                    resetDrawable()
                }
                if (message == "left"){
                    iv_direction?.setImageResource(R.drawable.baseline_arrow_back_24)
                    resetDrawable()
                }
                if (message == "up"){
                    iv_direction?.setImageResource(R.drawable.baseline_arrow_upward_24)
                    resetDrawable()
                }
                if (message == "down"){
                    iv_direction?.setImageResource(R.drawable.baseline_arrow_downward_24)
                    resetDrawable()
                }
                if (message == "far"){
                    iv_direction?.setImageResource(R.drawable.baseline_brightness_low_24)
                    resetDrawable()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the app has notification access & if not, ask the user to open settings
        NotificationPermissionCheck.checkNotificationAccess(this)

        // Start the MQTT background service
        val serviceIntent = Intent(this, MQTTService::class.java)
        startService(serviceIntent)

        // Register the BroadcastReceiver
        val intentFilter = IntentFilter("MQTTMessageReceived")
        registerReceiver(mqttMessageReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)

        // Setup UI
        setupUI()

    }

    private fun publishMessage(topic: String, content: String) {
        val serviceIntent = Intent(this, MQTTService::class.java)
        serviceIntent.putExtra("action", "publish")
        serviceIntent.putExtra("topic", topic)
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
            setRedLight(red)
        }

        btn_yellow = findViewById(R.id.btn_yellow)
        btn_yellow?.setOnClickListener {
            yellow = !yellow
            setYellowLight(yellow)
        }

        btn_green = findViewById(R.id.btn_green)
        btn_green?.setOnClickListener {
            green = !green
            setGreenLight(green)
        }

        tv_light?.text = "Light reading: 0"
    }

    fun setRedLight(flag: Boolean) {
        if (flag){
            iv_red?.setColorFilter(Color.RED)
            publishMessage("a00300334/led/red", "1")
        }else{
            iv_red?.setColorFilter(Color.GRAY)
            publishMessage("a00300334/led/red", "0")
        }
    }

    fun setYellowLight(flag: Boolean) {
        if (flag){
            iv_yellow?.setColorFilter(Color.YELLOW)
            publishMessage("a00300334/led/yellow", "1")
        }else{
            iv_yellow?.setColorFilter(Color.GRAY)
            publishMessage("a00300334/led/yellow", "0")
        }
    }

    fun setGreenLight(flag: Boolean) {
        if (flag){
            iv_green?.setColorFilter(Color.GREEN)
            publishMessage("a00300334/led/green", "1")
        }else{
            iv_green?.setColorFilter(Color.GRAY)
            publishMessage("a00300334/led/green", "0")
        }
    }

    /**
     * Reset the direction drawable after 2 seconds
     */
    fun resetDrawable(){
        Handler(Looper.getMainLooper()).postDelayed({
            iv_direction?.setImageResource(R.drawable.empty)
        }, 2000)
    }
}

