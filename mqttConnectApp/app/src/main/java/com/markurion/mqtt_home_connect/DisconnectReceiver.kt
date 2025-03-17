package com.markurion.mqtt_home_connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DisconnectReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DisconnectReceiver", "Disconnect action received")
        // Kill the entire application
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}