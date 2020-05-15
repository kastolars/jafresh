package com.kastolars.expirationreminderproject.models

import android.content.ContentValues
import android.database.Cursor
import java.time.Instant
import java.util.*

class Item(val uuid: UUID, val name: String, val expirationDate: Date) {

    constructor(cursor: Cursor) : this(
        UUID.fromString(cursor.getString(cursor.getColumnIndex("uuid"))),
        cursor.getString(cursor.getColumnIndex("name")),
        Date.from(
            Instant.ofEpochMilli(cursor.getLong(cursor.getColumnIndex("expiration_date")))
        )
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Item

        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (expirationDate != other.expirationDate) return false
        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + expirationDate.hashCode()
        return result
    }

    // Create
    fun getContentValues(): ContentValues {
        val cv = ContentValues()
        cv.put("uuid", uuid.toString())
        cv.put("name", name)
        cv.put("expiration_date", expirationDate.time)
        return cv
    }
}