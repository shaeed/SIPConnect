package com.shaeed.fcmclient.data

import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    fun getAll(): Flow<List<CallLog>>
    suspend fun deleteOlderThan(threshold: Long)
}
