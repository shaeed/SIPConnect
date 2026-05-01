package com.shaeed.fcmclient.data

import kotlinx.coroutines.flow.Flow

class RoomCallLogRepository(private val dao: CallLogDao) : CallLogRepository {
    override fun getAll(): Flow<List<CallLog>> = dao.getAll()
    override suspend fun deleteOlderThan(threshold: Long) = dao.deleteOlderThan(threshold)
}
