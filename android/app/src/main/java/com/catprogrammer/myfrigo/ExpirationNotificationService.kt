package com.catprogrammer.myfrigo

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PersistableBundle
import android.support.v4.app.NotificationCompat
import com.catprogrammer.myfrigo.model.Food
import com.catprogrammer.myfrigo.model.GeneralCallback
import com.catprogrammer.myfrigo.util.HttpUtil
import com.catprogrammer.myfrigo.util.SharedPreferencesUtil
import com.google.gson.JsonObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


private const val NOTIFICATION_ID = 233
private const val CHANNEL_ID = "com.catprogrammer.myfrigo.notification.chanel.expiration"


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
class ExpirationNotificationService : JobService() {

    override fun onStopJob(p0: JobParameters?): Boolean {
        // true to indicate to the JobManager whether you'd like to reschedule this job
        // based on the retry criteria provided at job creation-time;
        // or false to end the job entirely.
        // Regardless of the value returned, your job must stop executing.
        return false
    }

    override fun onStartJob(p0: JobParameters?): Boolean {


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
                    // send notification
                    sendNotification(foods)
                    // save food list to shared preference
                    SharedPreferencesUtil.writeFoods(this@ExpirationNotificationService, foods)
                }

                // schedule job for tomorrow
                if(p0 != null) {
                    val bundle = p0.extras
                    val scheduler = this@ExpirationNotificationService.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    val jobService = ComponentName(this@ExpirationNotificationService, ExpirationNotificationService::class.java)

                    ExpirationNotificationService.scheduleNextJob(
                            bundle.getInt("hour"), bundle.getInt("minute"), scheduler, jobService)
                }

                // true if this job should be rescheduled according to the
                // back-off criteria specified when it was first scheduled;
                // false otherwise.
                jobFinished(p0, false)
            }

            override fun onFailure() {
                jobFinished(p0, true)
            }
        }

        HttpUtil.getInstance().getAllFoods(cb)

        //	true if your service will continue running, using a separate thread when appropriate.
        // false means that this job has completed its work.
        return true
    }


    private fun sendNotification(foods: List<Food>) {
        var contentText: String?
        val names = ArrayList<String>()
        for (f in foods) {
            if (!f.name.isEmpty()) {
                names.add(f.name)
            }
        }

        contentText = when (names.size) {
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

        if (names.size != foods.size && names.size != 0) {
            contentText += "..."
        }

        val mBuilder = NotificationCompat.Builder(this@ExpirationNotificationService, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("${foods.size}个食物要过期啦")

        if (contentText != null) mBuilder.setContentText(contentText)

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
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build())
    }


    companion object {

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

        @JvmStatic
        fun scheduleJobs(ctx: Context) {
            val scheduler = ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val jobService = ComponentName(ctx, ExpirationNotificationService::class.java)

            scheduler.cancelAll()
            scheduleNextJob(8, 0, scheduler, jobService)
            scheduleNextJob(17, 30, scheduler, jobService)
            scheduleNextJob(22, 0, scheduler, jobService)
        }

        @JvmStatic
        private fun scheduleNextJob(hour: Int, minute: Int, scheduler: JobScheduler, jobService: ComponentName) {
            val now = LocalDateTime.now()

            var target = now.withHour(hour).withMinute(minute).withSecond(0)
            if (target.isBefore(now)) {
                target = target.plusDays(1)
            }

            // the unique id by alarm time
            val hourStr = if (hour.toString().length == 2) hour.toString() else "0" + hour.toString()
            val minuteStr = if (minute.toString().length == 2) minute.toString() else "0" + minute.toString()
            val jobId = ("$NOTIFICATION_ID$hourStr$minuteStr").toInt()

            // millisecond until next time
            val milis = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()

            // bundle to pass to the job, contains hour and minute to schedule the next job
            val bundle = PersistableBundle()
            bundle.putInt("hour", hour)
            bundle.putInt("minute", minute)

            // build the job
            val jobInfo = JobInfo.Builder(jobId, jobService)
                    .setMinimumLatency(milis)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(bundle)
                    .build()

            // schedule the job
            scheduler.schedule(jobInfo)
        }
    }
}
