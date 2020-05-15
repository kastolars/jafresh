package com.kastolars.expirationreminderproject

import androidx.work.WorkInfo

interface WorkInfoStateHandler {
    fun handle(workinfo: WorkInfo?)
}
