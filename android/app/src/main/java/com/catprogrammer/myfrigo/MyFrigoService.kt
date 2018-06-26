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
        // register channel
        ExpirationNotificationService.createNotificationChannel(this)
        ExpirationNotificationService.startActionNotification(this)
        registerAlarm(8, 0)
        registerAlarm(17, 30)
        registerAlarm(22, 0)
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        throw NotImplementedError()
    }

    private fun registerAlarm(hour: Int, minute: Int) {
        //  alarm time
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        // alarm action
        val actionNotification = "com.catprogrammer.myfrigo.action.ACTION_NOTIFICATION"
        val intent = Intent(this, ExpirationNotificationServiceReceiver::class.java).apply {
            action = actionNotification
        }

        // the unique id by alarm time
        val hourStr = if (hour.toString().length == 2) hour.toString() else "0" + hour.toString()
        val minuteStr = if (minute.toString().length == 2) minute.toString() else "0" + minute.toString()
        val id = (hourStr + minuteStr).toInt()

        // set alarm
        val alarmMgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, alarmIntent)
    }
}
