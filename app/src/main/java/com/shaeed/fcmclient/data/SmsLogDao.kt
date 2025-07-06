package com.shaeed.fcmclient.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smsLog: SmsLog)

    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SmsLog>>
}
