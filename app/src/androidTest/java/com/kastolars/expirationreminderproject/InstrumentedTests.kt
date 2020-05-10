package com.kastolars.expirationreminderproject

import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentedTests {

    @get:Rule
    val mMainActivityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testLaunchItemActivity() {
        // Start item activity
        onView(withId(R.id.floating_action_button)).perform(click())

        // Check to see that name is blank
        onView(withId(R.id.edit_text)).check(matches(withText("")))
    }

    @Test
    fun testAddNewItem() {
        // Start item activity
        onView(withId(R.id.floating_action_button)).perform(click())

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
        val recyclerView = mMainActivityRule.activity.findViewById<RecyclerView>(R.id.item_list)
        assert(recyclerView.adapter?.itemCount == 1)
        val viewHolder: ItemListAdapter.ItemViewHolder = recyclerView.findViewHolderForAdapterPosition(0) as ItemListAdapter.ItemViewHolder
        val itemName = viewHolder.nameView.text.toString()
        val date = viewHolder.dateView.toString()

        // Assert on viewholder
        assert(itemName == "milk")
        assert(date == "12-7-2020")
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
        val viewHolder: ItemListAdapter.ItemViewHolder = recyclerView.findViewHolderForAdapterPosition(0) as ItemListAdapter.ItemViewHolder

        // TODO: swipe on the viewholder and check to see if it's still there
    }
}