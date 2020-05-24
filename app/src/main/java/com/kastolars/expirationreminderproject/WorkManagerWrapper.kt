package com.kastolars.expirationreminderproject

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import com.kastolars.expirationreminderproject.models.Item
import java.time.Duration
import java.util.*

class WorkManagerWrapper(context: Context) {

    private val tag = "exprem" + WorkManagerWrapper::class.simpleName
    private val mWorkManager = WorkManager.getInstance(context)

    // Enqueues all reminders relevant to given item
    fun enqueueReminders(
        item: Item,
        notificationTag: String
    ): ArrayList<UUID> {
        Log.v(tag, "enqueueReminders called")
        val cal = Calendar.getInstance(TimeZone.getDefault())

        // Get today's date
        val today = cal.time
        // Set calendar to expiration date
        val expirationDate = item.expirationDate
        cal.time = expirationDate
        cal.set(Calendar.HOUR_OF_DAY, 6)
        cal.set(Calendar.MINUTE, 0)

        // Collect all the workRequest UUIDs
        val workRequestIds = ArrayList<UUID>()

        // Iterate and enqueue reminders
        arrayOf(0, -1, -1, -5).forEach {
            cal.add(Calendar.DATE, it)
            if (cal.time.after(today)) {
                val id = enqueueReminder(today, cal.time, item, notificationTag)
                workRequestIds.add(id)
            }
        }

        return workRequestIds
    }

    // Enqueues a single reminder from the collection
    private fun enqueueReminder(
        today: Date,
        futureDate: Date,
        item: Item,
        notificationTag: String
    ): UUID {
        Log.v(tag, "enqueueReminder called")
        Log.d(tag, "Reminder will be executed on $futureDate")
        val differenceInMillis = futureDate.time - today.time
        val delay = Duration.ofMillis(differenceInMillis)
        val data = Data.Builder()
            .putString("uuid", item.uuid.toString())
            .putString("name", item.name)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay)
            .setInputData(data)
            .addTag(notificationTag)
            .build()
        mWorkManager.enqueue(oneTimeWorkRequest)
        return oneTimeWorkRequest.id
    }

    fun getWorkInfoById(id: UUID): ListenableFuture<WorkInfo> {
        return mWorkManager.getWorkInfoById(id)
    }

    fun getCount(workTag: String): Int {
        return mWorkManager.getWorkInfosByTag(workTag).get().size
    }

    fun getWorkInfoByIdLiveData(uuid: UUID): LiveData<WorkInfo> {
        return mWorkManager.getWorkInfoByIdLiveData(uuid)
    }

    fun getWorkInfosByTag(workTag: String): ListenableFuture<MutableList<WorkInfo>> {
        return mWorkManager.getWorkInfosByTag(workTag)

    }

    fun cancelWorkById(id: UUID): Operation {
        return mWorkManager.cancelWorkById(id)
    }

    fun clear(): Operation {
        return mWorkManager.pruneWork()
    }
}