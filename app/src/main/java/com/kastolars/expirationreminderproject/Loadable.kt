package com.kastolars.expirationreminderproject

import android.content.ContentValues
import android.database.Cursor
import java.util.*

interface Loadable {
    fun getContentValues(): ContentValues
    fun uuid(): UUID
}