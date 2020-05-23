package com.kastolars.expirationreminderproject

import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kastolars.expirationreminderproject.activities.MainActivity
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class InstrumentedTests {

    @get:Rule
    val mMainActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testAddNewItem() {

        // Get recycler view
        val recyclerView = mMainActivityRule.activity.findViewById<RecyclerView>(R.id.item_list)
        var itemCount = recyclerView.adapter?.itemCount!!

        // Assert that it's empty
        assertEquals(0, itemCount)

        // Start item activity
        onView(withId(R.id.floating_action_button)).perform(click())

        // Check to see that name is blank
        onView(withId(R.id.edit_text)).check(matches(withText("")))

        // Type name
        onView(withId(R.id.edit_text)).perform(typeText("milk"))

        // Assert name
        onView(withId(R.id.edit_text)).check(matches(withText("milk")))

        // Click on set expiration date button
        onView(withId(R.id.set_expiration_date_button)).perform(click())

        // Select date
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
            PickerActions.setDate(2020, 12, 7)
        )

        // Finish dialog
        onView(withId(android.R.id.button1)).perform(click())

        // Assert date
        val expectedDate = String.format("12-7-2020")
        onView(withId(R.id.current_expiration_date)).check(matches(withText(expectedDate)))

        // Go back to main activity
        onView(withId(R.id.done_button)).perform(click())

        // Check the recycler view
        itemCount = recyclerView.adapter?.itemCount!!
        assertEquals(itemCount, 1)
        val viewHolder: ItemListAdapter.ItemViewHolder =
            recyclerView.findViewHolderForAdapterPosition(0) as ItemListAdapter.ItemViewHolder
        val itemName = viewHolder.nameView.text.toString()
        val date = viewHolder.dateView.text.toString()

        // Assert on viewholder
        assertEquals("milk", itemName)
        assertEquals("12-07-2020:06", date)

        // Assert on work manager
        val workTag = "JaFresh"
        val workManager = mMainActivityRule.activity.getWorkManagerWrapper()
        val workRequestCount = workManager.getCount(workTag)
        assertEquals(workRequestCount, 4)

        // Assert on observers
        var workInfos = workManager.getWorkInfosByTag(workTag)
        workInfos.get().forEach {
            val id = it.id
            val liveData = workManager.getWorkInfoByIdLiveData(id)
            // TODO: figure out why this data does not have observers
//            assertTrue(liveData.hasObservers())
        }

        Espresso.pressBackUnconditionally()
        with(mMainActivityRule) {
            finishActivity()
            launchActivity(null)
        }

        // Assert on item count once again
        itemCount = recyclerView.adapter?.itemCount!!
        assertEquals(1, itemCount)

        // Assert that observers have been restored
        workInfos = workManager.getWorkInfosByTag(workTag)
        workInfos.get().forEach {
            val id = it.id
            val liveData = workManager.getWorkInfoByIdLiveData(id)
//            assertTrue(liveData.hasObservers())
        }
    }

    @Test
    fun testNotification() {
        val ctx = mMainActivityRule.activity.applicationContext
        val workManager = WorkManager.getInstance(ctx)
        workManager.cancelAllWork()
        workManager.pruneWork()
        var data = Data.Builder()
            .putString("uuid", "123")
            .putString("name", "test")
            .build()
        var oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(Duration.ofSeconds(1))
            .setInputData(data)
            .addTag("Test")
            .build()
        workManager.enqueue(oneTimeWorkRequest)
        Thread.sleep(1000)
        data = Data.Builder()
            .putString("uuid", "123")
            .putString("name", "test2")
            .build()
        oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(Duration.ofSeconds(1))
            .setInputData(data)
            .addTag("Test")
            .build()
        workManager.enqueue(oneTimeWorkRequest)
        assertEquals(2, workManager.getWorkInfosByTag("Test").get().size)
        Thread.sleep(5000)
    }

    @Test
    fun testRemoveItem() {
        // Start item activity
        onView(withId(R.id.floating_action_button)).perform(click())

        // Type name
        onView(withId(R.id.edit_text)).perform(typeText("milk"))

        // Click on set expiration date button
        onView(withId(R.id.set_expiration_date_button)).perform(click())

        // Select date
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
            PickerActions.setDate(2020, 12, 7)
        )

        // Finish dialog
        onView(withId(android.R.id.button1)).perform(click())

        // Go back to main activity
        onView(withId(R.id.done_button)).perform(click())

        // Check the recycler view
        val recyclerView = mMainActivityRule.activity.findViewById<RecyclerView>(R.id.item_list)
        assert(recyclerView.adapter?.itemCount == 1)
        val viewHolder: ItemListAdapter.ItemViewHolder =
            recyclerView.findViewHolderForAdapterPosition(0) as ItemListAdapter.ItemViewHolder

        // TODO: swipe on the viewholder and check to see if it's still there
//        onView().perform(swipeRight())
    }
}