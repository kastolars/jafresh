package com.kastolars.expirationreminderproject

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import java.time.Duration
import java.util.*

class WorkManagerWrapper(context: Context) {

    private val tag = "exprem" + WorkManagerWrapper::class.simpleName
    private val workManager = WorkManager.getInstance(context)


    fun enqueueReminders(item: Item): ArrayList<UUID> {
        Log.v(tag, "enqueueReminders called")
        val cal = Calendar.getInstance(TimeZone.getDefault())

        // Get today's date
        val today = cal.time
        // Set calendar to expiration date
        val expirationDate = item.expirationDate
        cal.time = expirationDate

        // Collect all the workRequest UUIDs
        val workRequestIds = ArrayList<UUID>()

        // Iterate and enqueue reminders
        arrayOf(0, -1, -1, -5).forEach {
            cal.add(Calendar.DATE, it)
            if (cal.time.after(today)) {
                val id = enqueueReminder(today, cal.time, item)
                workRequestIds.add(id)
            }
        }

        return workRequestIds
    }

    private fun enqueueReminder(
        today: Date,
        futureDate: Date,
        item: Item
    ): UUID {
        val differenceInMillis = futureDate.time - today.time
        val delay = Duration.ofMillis(differenceInMillis)
        val data = Data.Builder()
            .putString("uuid", item.uuid.toString())
            .putString("name", item.name)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay)
            .setInputData(data)
            .build()
        workManager.enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }

    fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
        return workManager.getWorkInfoById(id)
    }
}