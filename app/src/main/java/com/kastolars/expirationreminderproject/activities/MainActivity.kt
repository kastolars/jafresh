package com.kastolars.expirationreminderproject.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.LayoutAnimationController
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kastolars.expirationreminderproject.*
import com.kastolars.expirationreminderproject.models.Item
import com.kastolars.expirationreminderproject.models.Notification
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), LifecycleOwner,
    WorkInfoStateHandler {

    private val mNotificationTag = "JaFresh"
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ItemListAdapter
    private lateinit var mItems: ArrayList<Item>
    private val NEW_ITEM = 1;
    private val tag = "exprem" + MainActivity::class.simpleName
    private val mDatabaseHelper: DatabaseHelper =
        DatabaseHelper(this)
    private val mWorkManagerWrapper =
        WorkManagerWrapper(this)
    private val observer =
        LiveDataObserver(this)


    fun getWorkManagerWrapper(): WorkManagerWrapper {
        return mWorkManagerWrapper
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(tag, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize list
        mItems = mDatabaseHelper.fetchAllItems()
        mItems.sortBy { it.expirationDate }
        mAdapter =
            ItemListAdapter(mItems)
        mRecyclerView = findViewById(R.id.item_list)
        val layoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.layoutManager = layoutManager
        ItemTouchHelper(swipeItemCallback).attachToRecyclerView(mRecyclerView)
        mRecyclerView.adapter = mAdapter
        val dividerItemDecoration =
            DividerItemDecoration(mRecyclerView.context, layoutManager.orientation)
        mRecyclerView.addItemDecoration(dividerItemDecoration)
        val set = AnimationSet(true)
        val anim = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        anim.duration = 200
        set.addAnimation(anim)

        val controller = LayoutAnimationController(set, 0.5f)
        mRecyclerView.layoutAnimation = controller

        val itemCount = mAdapter.itemCount
        Log.d(tag, "Item count: $itemCount")

        // Get work information
        val workInfos = mWorkManagerWrapper.getWorkInfosByTag(mNotificationTag).get()
        workInfos.forEach {
            val id = it.id
            val livedata = mWorkManagerWrapper.getWorkInfoByIdLiveData(id)
            livedata.observe(this, observer)
        }
        Log.d(tag, "Work info count: ${workInfos.size}")

        // Create notification channel
        createNotificationChannel()

        // Initialize button
        val mButton: FloatingActionButton = findViewById(R.id.floating_action_button)
        mButton.setOnClickListener {
            Log.d(tag, "Floating action button pressed")
            val intent = Intent(applicationContext, ItemActivity::class.java)
            startActivityForResult(intent, NEW_ITEM)
        }

        // Initialize OCR Launch Button
        val mOcrLaunchButton: FloatingActionButton = findViewById(R.id.ocr_launch_button)
        mOcrLaunchButton.setOnClickListener {
            Log.d(tag, "Floating action button pressed (OCR Launch)")
            val intent = Intent(applicationContext, OcrCaptureActivity::class.java)
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
            val item =
                Item(
                    uuid,
                    name!!,
                    date
                )
            Log.d(tag, "New item: $item")

            // Enqueue reminders
            val workRequestUuids = mWorkManagerWrapper.enqueueReminders(item, mNotificationTag)
            workRequestUuids.forEach {
                val notification =
                    Notification(
                        it,
                        uuid
                    )
                mDatabaseHelper.insertNotification(notification)
            }

            // Attach observers
            workRequestUuids.forEach {
                val liveData = mWorkManagerWrapper.getWorkInfoByIdLiveData(it)
                liveData.observe(this, observer)
            }

            // Populate database
            val id = mDatabaseHelper.insertItem(item)
            if (id == -1L) {
                Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show()
            }

            // Update list
            mItems.add(item)
            mItems.sortBy { it.expirationDate }
            mAdapter.notifyDataSetChanged()

            val itemCount = mAdapter.itemCount
            Log.d(tag, "Item count: $itemCount")

            // First time users
            val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val firstStart = prefs.getBoolean("firstStart", true)

            if (firstStart) {
                showFirstTimeAddItemDialog()
            }
        }
    }

    private fun showFirstTimeAddItemDialog() {
        Log.v(tag, "showFirstTimeAddItemDialog called")
        val img = ImageView(this)
        img.setImageResource(R.drawable.swipe)
        AlertDialog.Builder(this)
            .setTitle("Note")
            .setMessage("You can swipe right on an item to delete it")
            .setView(img)
            .setCancelable(false)
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }.create().show()

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("firstStart", false)
        editor.apply()
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

    // Handle state of reminders being observed
    override fun handle(workinfo: WorkInfo?) {
        Log.v(tag, "handle called")
        when (workinfo?.state) {
            WorkInfo.State.SUCCEEDED -> {
                val uuid = workinfo.id
                Log.v(tag, "Work with uuid $uuid succeeded")
                mDatabaseHelper.deleteNotification(uuid)
                mWorkManagerWrapper.clear()
            }
            WorkInfo.State.CANCELLED -> {
                val uuid = workinfo.id
                Log.v(tag, "Work with uuid $uuid cancelled")
                mDatabaseHelper.deleteNotification(uuid)
                mWorkManagerWrapper.clear()
            }
            WorkInfo.State.ENQUEUED -> {
                val uuid = workinfo.id
                Log.v(tag, "Work with uuid $uuid enqueued")
            }
            else -> {
            }
        }
    }

    // Handles swiping on the element
    private val swipeItemCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                Log.v(tag, "onMove called")
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.v(tag, "onSwiped called")
                val item = mItems.removeAt(viewHolder.adapterPosition)
                mItems.sortBy { it.expirationDate }
                mDatabaseHelper.deleteItem(item)
                val notifications = mDatabaseHelper.fetchNotificationsByItemUuid(item.uuid)
                notifications.forEach {
                    mWorkManagerWrapper.cancelWorkById(it.workRequestUuid)
                }
                mAdapter.notifyDataSetChanged()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                Log.v(tag, "onChildDraw called")
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val view = viewHolder.itemView
                    val p = Paint()
                    p.color = Color.RED
                    val icon = BitmapFactory.decodeResource(
                        applicationContext.resources, R.drawable.delete
                    )
                    val px =
                        (16 * (resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
                    if (dX > 0) {
                        c.drawRect(
                            view.left.toFloat(), view.top.toFloat(), dX,
                            view.bottom.toFloat(), p
                        )

                        c.drawBitmap(
                            icon,
                            view.left.toFloat() + px,
                            view.top.toFloat() + (view.bottom - view.top - icon.height) / 2,
                            p
                        )
                    } else {
                        c.drawRect(
                            view.right + dX, view.top.toFloat(),
                            view.right.toFloat(), view.bottom.toFloat(), p
                        )

                        c.drawBitmap(
                            icon,
                            view.right.toFloat() - px - icon.width.toFloat(),
                            view.top.toFloat() + (view.bottom - view.top - icon.height) / 2,
                            p
                        )
                    }
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
}
