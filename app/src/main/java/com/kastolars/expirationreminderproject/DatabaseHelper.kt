package com.kastolars.expirationreminderproject

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.kastolars.expirationreminderproject.models.Item
import com.kastolars.expirationreminderproject.models.Notification
import java.util.*
import kotlin.collections.ArrayList

class DatabaseHelper(
    context: Context?
) : SQLiteOpenHelper(context, "ExpirationReminder.db", null, 2) {

    private val expirationDateColumn = "expiration_date"
    private val tag = "exprem" + DatabaseHelper::class.simpleName
    private val itemTable = "items"
    private val notificationsTable = "notifications"
    private val uuidColumn = "uuid"
    private val itemUuidColumn = "item_uuid"
    private val nameColumn = "name"

    override fun onCreate(db: SQLiteDatabase?) {
        Log.v(tag, "onCreate called")
        createItemTable(db)
        createNotificationsTable(db)
    }

    private fun createNotificationsTable(db: SQLiteDatabase?) {
        Log.v(tag, "createNotificationsTable called")
        val sql = """
            CREATE TABLE $notificationsTable (
            $uuidColumn TEXT NOT NULL,
            $itemUuidColumn TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(sql)
    }

    private fun createItemTable(db: SQLiteDatabase?) {
        Log.v(tag, "createItemTable called")
        val sql = """
            CREATE TABLE $itemTable (
            $uuidColumn TEXT NOT NULL,
            $nameColumn TEXT NOT NULL, 
            $expirationDateColumn INTEGER NOT NULL
            )
        """.trimIndent()
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.v(tag, "onUpgrade called")
        var sql = """
            DROP TABLE IF EXISTS $itemTable
        """.trimIndent()
        db?.execSQL(sql)
        sql = """
            DROP TABLE IF EXISTS $notificationsTable
        """.trimIndent()
        db?.execSQL(sql)
        onCreate(db)
    }

    // Create
    fun insertItem(item: Item): Long {
        Log.v(tag, "insertItem called")
        val db = writableDatabase
        val cv = item.getContentValues()
        return db.insert(itemTable, null, cv)
    }

    // Create
    fun insertNotification(notification: Notification): Long {
        Log.v(tag, "insertNotification called")
        val db = writableDatabase
        val cv = notification.getContentValues()
        return db.insert(notificationsTable, null, cv)
    }

    // Read
    fun fetchItemByUuid(uuid: UUID): Item? {
        Log.v(tag, "fetchItemByUuid called")
        val db = readableDatabase
        val cursor = db.query(
            true,
            itemTable,
            null,
            "uuid=?",
            arrayOf(uuid.toString()),
            null,
            null,
            null,
            null
        )
        val item = if (cursor.moveToFirst()) {
            Item(cursor)
        } else {
            null
        }
        cursor.close()
        return item
    }

    // Read
    fun fetchNotificationByUuid(uuid: UUID): Notification? {
        Log.v(tag, "fetchNotificationByUuid called")
        val db = readableDatabase
        val cursor = db.query(
            true,
            notificationsTable,
            null,
            "uuid=?",
            arrayOf(uuid.toString()),
            null,
            null,
            null,
            null
        )
        val notification = if (cursor.moveToFirst()) {
            Notification(cursor)
        } else {
            null
        }
        cursor.close()
        return notification
    }

    fun fetchNotificationsByItemUuid(itemUuid: UUID): ArrayList<Notification> {
        Log.v(tag, "fetchNotificationsByItemUuid called")
        val notifications = ArrayList<Notification>()
        val db = readableDatabase
        val cursor = db.query(
            notificationsTable,
            null,
            "item_uuid=?",
            arrayOf(itemUuid.toString()),
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            val notification =
                Notification(
                    cursor
                )
            notifications.add(notification)
        }
        cursor.close()
        return notifications
    }

    // Update
    fun updateItem(item: Item): Int {
        Log.v(tag, "updateItem called")
        val db = writableDatabase
        val cv = item.getContentValues()
        return db.update(itemTable, cv, "$uuidColumn=?", arrayOf(item.uuid.toString()))
    }

    // Delete
    fun deleteItem(item: Item): Int {
        Log.v(tag, "deleteItem called")
        val db = writableDatabase
        return db.delete(itemTable, "$uuidColumn=?", arrayOf(item.uuid.toString()))
    }

    // Delete
    fun deleteNotification(uuid: UUID): Int {
        Log.v(tag, "deleteNotification called")
        val db = writableDatabase
        return db.delete(notificationsTable, "$uuidColumn=?", arrayOf(uuid.toString()))
    }

    // Read array
    fun fetchAllItems(): ArrayList<Item> {
        Log.v(tag, "fetchAllItems called")
        val items = ArrayList<Item>()
        val query = "SELECT * FROM $itemTable"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val item =
                Item(cursor)
            items.add(item)
        }
        cursor.close()
        return items
    }
}