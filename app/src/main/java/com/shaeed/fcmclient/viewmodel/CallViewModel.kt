package com.shaeed.fcmclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.fcmclient.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).callLogDao()

    val callLogs = dao.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun deleteOldCallLogs() {
        viewModelScope.launch {
            val threeMonthsAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
            dao.deleteOlderThan(threeMonthsAgo)
        }
    }
}