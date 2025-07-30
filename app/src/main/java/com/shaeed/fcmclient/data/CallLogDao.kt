package com.shaeed.fcmclient.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callLog: CallLog)

    @Query("SELECT * FROM call_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CallLog>>

    @Query("DELETE FROM call_log WHERE timestamp < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("DELETE FROM call_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}
