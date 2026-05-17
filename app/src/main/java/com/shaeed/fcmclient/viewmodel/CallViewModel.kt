package com.shaeed.fcmclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.fcmclient.data.CallLogRepository
import com.shaeed.fcmclient.data.GroupedCallLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CallViewModel(private val repository: CallLogRepository) : ViewModel() {

    val callLogs = repository.getAll().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val groupedCallLogs = repository.getAll()
        .map { logs ->
            logs.groupBy { it.normalizedNumber }
                .map { (_, entries) ->
                    val sorted = entries.sortedByDescending { it.timestamp }
                    GroupedCallLog(
                        normalizedNumber = sorted.first().normalizedNumber,
                        phoneNumber = sorted.first().phoneNumber,
                        count = sorted.size,
                        latestTimestamp = sorted.first().timestamp,
                        latestStatus = sorted.first().status,
                        calls = sorted
                    )
                }
                .sortedByDescending { it.latestTimestamp }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteOldCallLogs() {
        viewModelScope.launch {
            val threeMonthsAgo = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
            repository.deleteOlderThan(threeMonthsAgo)
        }
    }
}
