package com.kastolars.expirationreminderproject

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.kastolars.expirationreminderproject.models.Item
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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
        // Arrange
        val name = "milk"
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.add(Calendar.DATE, 10)
        val expirationDate = cal.time
        val uuid = UUID.randomUUID()
        val item = Item(
            uuid,
            name,
            expirationDate
        )
        val data = Data.Builder()
            .putString("uuid", uuid.toString())
            .build()
        val testDriver = WorkManagerTestInitHelper.getTestDriver(getApplicationContext())
        // Act
        val ids = workManagerWrapper.enqueueReminders(item, "testTag")
        // Assert
        assert(ids.size == 4)
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

    private fun mockLifecycleOwner(): LifecycleOwner? {
        val owner: LifecycleOwner = Mockito.mock(LifecycleOwner::class.java)
        val lifecycle = LifecycleRegistry(owner)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        Mockito.`when`(owner.lifecycle).thenReturn(lifecycle)
        return owner
    }

    @Test
    fun testObservers() {
        // Arrange
        val name = "milk"
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.add(Calendar.DATE, 10)
        val expirationDate = cal.time
        val uuid = UUID.randomUUID()
        val item = Item(
            uuid,
            name,
            expirationDate
        )
        val data = Data.Builder()
            .putString("uuid", uuid.toString())
            .build()
        val ids = workManagerWrapper.enqueueReminders(item, "testTag")
        val mockLifecycleOwner = mockLifecycleOwner()!!
        var isEnqueued = false
        var succeeded = false
        val stateHandler = object : WorkInfoStateHandler {
            override fun handle(workinfo: WorkInfo?) {
                val state = workinfo?.state!!
                when (state) {
                    WorkInfo.State.ENQUEUED -> {
                        isEnqueued = true
                        succeeded = false
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        isEnqueued = false
                        succeeded = true
                    }
                }
            }
        }
        val observer = LiveDataObserver(stateHandler)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(getApplicationContext())

        // Act
        ids.forEach {
            val liveData = workManagerWrapper.getWorkInfoByIdLiveData(it)
            liveData.observe(mockLifecycleOwner, observer)
            assertTrue(liveData.hasObservers())
            assertTrue(isEnqueued)
            assertFalse(succeeded)
            testDriver?.setInitialDelayMet(it)
            assertFalse(isEnqueued)
            assertTrue(succeeded)
        }
    }

    // TODO: test cancellation (on workmanager and observers)
}