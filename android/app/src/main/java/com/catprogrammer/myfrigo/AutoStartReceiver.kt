package com.catprogrammer.myfrigo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutoStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            context.startForegroundService(Intent(context, MyFrigoService::class.java))
        }
    }
}
