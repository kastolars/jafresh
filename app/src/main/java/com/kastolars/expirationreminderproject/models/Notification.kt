package com.kastolars.expirationreminderproject.models

import android.content.ContentValues
import android.database.Cursor
import java.util.*

class Notification(val workRequestUuid: UUID, val itemUuid: UUID) {

    constructor(cursor: Cursor) : this(
        UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
        UUID.fromString(cursor.getString(cursor.getColumnIndex("item_uuid")))
    )

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