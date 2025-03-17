package com.markurion.mqtt_home_connect

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class NotificationPermissionCheck {
    // Companion object to hold static methods for checking notification access
    // in java, this would be a static class
    companion object {
        fun checkNotificationAccess(context: Context){
            val notificationsEnabled = this.areNotificationsEnabled(context);
            if (!notificationsEnabled) {
                askUserToOpenNotificationSettings(context)
            }
        }

        fun areNotificationsEnabled(context: Context): Boolean {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.areNotificationsEnabled()
        }

        private fun askUserToOpenNotificationSettings(context: Context) {
            AlertDialog.Builder(context)
                .setTitle("Notification Access")
                .setMessage("This app needs notification access to receive MQTT messages.")
                .setPositiveButton("Open Settings") { _, _ ->
                    openNotificationSettings(context)
                }
                .setNegativeButton("No go away") { dialog, _ ->
                    showErrorDialog(context)
                }
                .show()
        }

        // close the app if the user denies notification access
        private fun showErrorDialog(context: Context) {
            AlertDialog.Builder(context)
                .setTitle("Notification Access")
                .setMessage("This app needs notification access to receive MQTT messages.")
                .setPositiveButton("Quit") { _, _ ->
                    (context as MainActivity).finish()
                }
                .show()
        }

        private fun openNotificationSettings(context: Context) {
            val intent =
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                }
            context.startActivity(intent)
        }

    }
}