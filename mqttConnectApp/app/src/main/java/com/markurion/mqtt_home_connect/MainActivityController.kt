package com.markurion.mqtt_home_connect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import android.widget.TextView

class MainActivityController private constructor(){
    private var context: Context? = null

    private var iv_red: ImageView? = null
    private var iv_yellow: ImageView? = null
    private var iv_green: ImageView? = null
    private var tv_light: TextView? = null


    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: MainActivityController? = null
        fun getInstance(): MainActivityController {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MainActivityController().also { INSTANCE = it }
            }
        }

        fun setContext(context: Context) {
            INSTANCE?.context = context
        }
    }


}