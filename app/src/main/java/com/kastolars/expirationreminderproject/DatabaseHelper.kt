package com.kastolars.expirationreminderproject

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

class DatabaseHelper(
    context: Context?
) : SQLiteOpenHelper(context, "ExpirationReminder.db", null, 1) {

    private val expirationDateColumn = "expiration_date"
    private val tag = "exprem" + DatabaseHelper::class.simpleName
    private val itemTable = "items"
    private val notificationsTable = "notifications"
    private val uuidColumn = "uuid"
    private val itemUuid = "item_uuid"
    private val nameColumn = "name"

    override fun onCreate(db: SQLiteDatabase?) {
        Log.v(tag, "onCreate called")
        var sql = """
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
        val sql = """
            DROP TABLE IF EXISTS $itemTable
        """.trimIndent()
        db?.execSQL(sql)
        onCreate(db)
    }

    // Create
    fun insertItem(loadable: Loadable): Long {
        Log.v(tag, "insertItem called")
        val db = writableDatabase
        val cv = loadable.getContentValues()
        val tableName = tableNameFromLoadable(loadable)
        return db.insert(tableName, null, cv)
    }

    // Read
    inline fun <reified T> fetchItemByUuid(uuid: UUID): T? {
        val db = readableDatabase
        val tableName = when (T::class) {
            Item::class -> "items"
            Notification::class -> "notifications"
            else -> throw IllegalArgumentException()
        }
        val cursor = db.query(
            true,
            tableName,
            null,
            "uuid=?",
            arrayOf(uuid.toString()),
            null,
            null,
            null,
            null
        )
        val item = if (cursor.moveToFirst()) {
            when (T::class) {
                Item::class -> Item(cursor)
                else -> throw IllegalArgumentException()
            }
        } else {
            null
        }
        cursor.close()
        return item as T
    }

    // Update
    fun updateItem(loadable: Loadable): Int {
        Log.v(tag, "updateItem called")
        val db = writableDatabase
        val cv = loadable.getContentValues()
        val tableName = tableNameFromLoadable(loadable)
        return db.update(tableName, cv, "$uuidColumn=?", arrayOf(loadable.uuid().toString()))
    }

    // Delete
    fun deleteItem(loadable: Loadable): Int {
        Log.v(tag, "deleteItem called")
        val db = writableDatabase
        val tableName = tableNameFromLoadable(loadable)
        return db.delete(tableName, "$uuidColumn=?", arrayOf(loadable.uuid().toString()))
    }

    // Read array
    inline fun <reified T> fetchAllItems(): ArrayList<Loadable> {
        val loadables = ArrayList<Loadable>()
        val tableName = when (T::class) {
            Item::class -> "items"
            Notification::class -> "notifications"
            else -> throw IllegalArgumentException()
        }
        val query = "SELECT * FROM $tableName"
        val db = readableDatabase
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val loadable = when (T::class) {
                    Item::class -> Item(cursor)
                    else -> java.lang.IllegalArgumentException()
                }
                loadables.add(loadable as Loadable)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return loadables
    }

    private fun tableNameFromLoadable(loadable: Loadable): String {
        return when (loadable::class) {
            Item::class -> itemTable
            Notification::class -> notificationsTable
            else -> throw java.lang.IllegalArgumentException()
        }
    }
}