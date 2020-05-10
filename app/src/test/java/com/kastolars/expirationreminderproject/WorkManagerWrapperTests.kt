package com.kastolars.expirationreminderproject

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
class WorkManagerWrapperTests {
    private lateinit var workManagerWrapper: WorkManagerWrapper
    private lateinit var config: Configuration
    private lateinit var targetContext: Context

    @Before
    fun setup() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(targetContext, config)
        workManagerWrapper = WorkManagerWrapper(getApplicationContext())
    }

    @Test
    fun testEnqueueReminders() {
        val name = "milk"
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.add(Calendar.DATE, 10)
        val expirationDate = cal.time
        val uuid = UUID.randomUUID()
        val item = Item(uuid, name, expirationDate)
        val ids = workManagerWrapper.enqueueReminders(item)
        val data = Data.Builder()
            .putString("uuid", uuid.toString())
            .build()
        assert(ids.size == 4)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(getApplicationContext())
        ids.forEach {
            var workInfo = workManagerWrapper.getWorkInfoById(it).get()
            assert(workInfo?.state == WorkInfo.State.ENQUEUED)
            testDriver?.setInitialDelayMet(it)
            workInfo = workManagerWrapper.getWorkInfoById(it).get()
            assert(workInfo?.state == WorkInfo.State.SUCCEEDED)
            val outputData = workInfo.outputData
            assert(outputData == data)
        }
    }
}