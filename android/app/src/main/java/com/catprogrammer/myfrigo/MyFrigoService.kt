package com.catprogrammer.myfrigo

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.util.*


class MyFrigoService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(0, Notification())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        registerAlarm()
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        throw NotImplementedError()
    }

    private fun registerAlarm() {
        // register channel
        ExpirationNotificationService.createNotificationChannel(this)
        ExpirationNotificationService.startActionNotification(this)

        // Set the alarm to start at 18:00 every day
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 17)
        calendar.set(Calendar.MINUTE, 15)

        val alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.

        val actionNotification = "com.catprogrammer.myfrigo.action.ACTION_NOTIFICATION"
        val intent = Intent(this, ExpirationNotificationServiceReceiver::class.java).apply {
            action = actionNotification
        }
        val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, alarmIntent)
    }
}
