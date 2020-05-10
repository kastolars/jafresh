package com.kastolars.expirationreminderproject

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mNotificationTag = "Expiration Reminder"
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ItemListAdapter
    private lateinit var mItems: java.util.ArrayList<Item>
    private val NEW_ITEM = 1;
    private val tag = "exprem" + MainActivity::class.simpleName
    private val mDatabaseHelper: DatabaseHelper = DatabaseHelper(this)
    private val mWorkManagerWrapper = WorkManagerWrapper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(tag, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize list
        mItems = mDatabaseHelper.fetchAllItems<Item>() as ArrayList<Item>
        mAdapter = ItemListAdapter(mItems)
        mRecyclerView = findViewById(R.id.item_list)
        val layoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = layoutManager
        ItemTouchHelper(SwipeItemCallback(mDatabaseHelper, mItems, mAdapter)).attachToRecyclerView(
            mRecyclerView
        )
        mRecyclerView.adapter = mAdapter

        // Create notification channel
        createNotificationChannel()

        // Initialize button
        val mButton: FloatingActionButton = findViewById(R.id.floating_action_button)
        mButton.setOnClickListener {
            Log.d(tag, "Floating action button pressed")
            val intent = Intent(applicationContext, ItemActivity::class.java)
            startActivityForResult(intent, NEW_ITEM)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(tag, "onActivityResult called")

        if (resultCode == Activity.RESULT_OK) {
            Log.d(tag, "Result code: $resultCode")

            // Construct item from intent
            val uuid = UUID.randomUUID()
            val name = data?.getStringExtra("name")
            val expirationDateSeconds = data?.getLongExtra("date", -1)
            val cal = Calendar.getInstance(TimeZone.getDefault())
            cal.timeInMillis = expirationDateSeconds!!
            val date = cal.time
            val item = Item(uuid, name!!, date)
            Log.d(tag, "New item: $item")

            mWorkManagerWrapper.enqueueReminders(item)

            // TODO: if id == -1, create a toast message
            val id = mDatabaseHelper.insertItem(item)

            // Update list
            mItems.add(item)
            mAdapter.notifyDataSetChanged()
        }
    }

    private fun createNotificationChannel() {
        Log.v(tag, "createNotificationChannel called")
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(
            mNotificationTag, mNotificationTag, importance
        )
        val description = "JaFresh Channel"
        notificationChannel.description = description
        notificationChannel.lightColor = Color.MAGENTA
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            notificationChannel
        )
    }
}
