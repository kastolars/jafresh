package com.kastolars.expirationreminderproject

import androidx.lifecycle.Observer
import androidx.work.WorkInfo

class LiveDataObserver(private val handler: WorkInfoStateHandler) : Observer<WorkInfo> {

    override fun onChanged(it: WorkInfo?) {
        handler.handle(it)
    }
}