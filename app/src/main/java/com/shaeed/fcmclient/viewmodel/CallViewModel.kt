package com.shaeed.fcmclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.fcmclient.data.CallLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallViewModel(private val repository: CallLogRepository) : ViewModel() {

    val callLogs = repository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun deleteOldCallLogs() {
        viewModelScope.launch {
            val threeMonthsAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
            repository.deleteOlderThan(threeMonthsAgo)
        }
    }
}
