package com.catprogrammer.myfrigo

import android.app.*
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.catprogrammer.myfrigo.util.HttpUtil
import com.google.gson.JsonObject
import java.time.LocalDate


private const val ACTION_NOTIFICATION = "com.catprogrammer.myfrigo.action.ACTION_NOTIFICATION"
private const val NOTIFICATION_ID_EXPIRATION = 233
private const val CHANNEL_ID = "com.catprogrammer.myfrigo.notification.chanel.expiration"


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
class ExpirationNotificationService : IntentService("ExpirationNotificationService") {

    override fun onCreate() {
        super.onCreate()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()

        startForeground(1, notification)
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_NOTIFICATION -> {
                handleActionNotification()
            }
        }
    }

    /**
     * Handle action Notification in the provided background thread
     */
    private fun handleActionNotification() {

        val cb: GeneralCallback = object : GeneralCallback() {
            override fun onSuccess(data: JsonObject) {
                val entries = data.entrySet() // foods
                val foods = ArrayList<Food>()

                for (map in entries) { // one food
                    val foodJson = map.value.asJsonObject
                    val food = Food.populateFood(foodJson)
                    if (food.expirationDate?.isEqual(LocalDate.now()) == true ||
                            food.expirationDate?.isBefore(LocalDate.now()) == true) {
                        foods.add(food)
                    }
                }

                foods.sortWith(compareBy(Food::expirationDate))
                if (foods.size > 0) {
                    sendNotification(foods)
                }
            }

            override fun onFailure() {

            }
        }// val cb end

        HttpUtil.getInstance().getAllFoods(cb)

    }

    private fun sendNotification(foods: List<Food>) {
        var contentText: String?
        val names = ArrayList<String>()
        for (f in foods) {
            if (!f.name.isEmpty()) {
                names.add(f.name)
            }
        }

        contentText = when(names.size) {
            0 -> null
            1 -> names[0]
            2 -> names[0] + "和" + names[1]
            else -> {
                var str = names[0]
                for (index in 1 until (names.size - 1)) {
                    str += "，" + names[index]
                }
                str += "和" + names[names.size - 1]
                str
            }
        }

        if(names.size != foods.size && names.size != 0) {
            contentText += "..."
        }

        val mBuilder = NotificationCompat.Builder(this@ExpirationNotificationService, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("${foods.size}个食物要过期啦")

        if(contentText != null) mBuilder.setContentText(contentText)

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(this, MainActivity::class.java)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(this)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)

        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Id allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID_EXPIRATION, mBuilder.build())
    }



    companion object {
        /**
         * Starts this service to perform action Noficifation
         * If the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        @JvmStatic
        fun startActionNotification(context: Context) {
            val intent = Intent(context, ExpirationNotificationService::class.java).apply {
                action = ACTION_NOTIFICATION
            }
            context.startForegroundService(intent)
        }

        @JvmStatic
        fun createNotificationChannel(ctx: Context) {
            val name = "食品过期通知"
            val description = "通知有食品马上要过期"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = ctx.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}
