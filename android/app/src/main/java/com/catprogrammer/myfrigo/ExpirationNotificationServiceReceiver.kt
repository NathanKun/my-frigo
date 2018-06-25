package com.catprogrammer.myfrigo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ExpirationNotificationServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // when the BroadcastReceiver is receiving an Intent broadcast
        // call startActionNotification
        ExpirationNotificationService.startActionNotification(context)
    }
}
