package com.shaeed.fcmclient.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCallLogRepository(initial: List<CallLog> = emptyList()) : CallLogRepository {
    private val _logs = MutableStateFlow(initial)

    override fun getAll(): Flow<List<CallLog>> = _logs

    override suspend fun deleteOlderThan(threshold: Long) {
        _logs.value = _logs.value.filter { it.timestamp >= threshold }
    }

    fun emit(logs: List<CallLog>) {
        _logs.value = logs
    }
}
