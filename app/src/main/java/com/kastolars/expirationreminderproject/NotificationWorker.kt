package com.kastolars.expirationreminderproject

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kastolars.expirationreminderproject.activities.MainActivity
import java.util.*

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val tag = "exprem" + NotificationWorker::class.java.simpleName
    private val mNotificationTag = "JaFresh"

    override fun doWork(): Result {
        Log.v(tag, "doWork called")
        return try {
            triggerNotification()
            val uuid = inputData.getString("uuid")
            val outputData = Data.Builder()
                .putString("uuid", uuid)
                .build()
            Result.success(outputData)
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun triggerNotification() {
        Log.v(tag, "triggerNotification called")
        val name = inputData.getString("name")
        val message = "This is a reminder about your item $name"
        val notification = NotificationCompat.Builder(applicationContext, mNotificationTag)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle("JaFresh")
            .setContentText(message)
            .setAutoCancel(true)
            .setColor(Color.GREEN)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    3,
                    Intent(applicationContext, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            ).build()!!
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        // Make sure there's a random id assigned
        val id = Random().nextInt()
        notificationManager.notify(id, notification)
        Log.d(tag, "Notification with id $id")
    }
}