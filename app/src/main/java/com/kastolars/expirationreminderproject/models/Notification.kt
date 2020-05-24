package com.kastolars.expirationreminderproject.models

import android.content.ContentValues
import android.database.Cursor
import java.util.*

// Represents a notification object for reminders
class Notification(val workRequestUuid: UUID, val itemUuid: UUID) {

    // Construct a notification from a database result
    constructor(cursor: Cursor) : this(
        UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
        UUID.fromString(cursor.getString(cursor.getColumnIndex("item_uuid")))
    )

    // For insertion into the database
    fun getContentValues(): ContentValues? {
        val cv = ContentValues()
        cv.put("uuid", workRequestUuid.toString())
        cv.put("item_uuid", itemUuid.toString())
        return cv
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Notification

        if (workRequestUuid != other.workRequestUuid) return false
        if (itemUuid != other.itemUuid) return false
        return true
    }

    override fun hashCode(): Int {
        var result = workRequestUuid.hashCode()
        result = 31 * result + itemUuid.hashCode()
        return result
    }
}