package com.shaeed.fcmclient.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE senderNormalized = :senderNormalized ORDER BY timestamp ASC")
    fun getMessagesForSender(senderNormalized: String): Flow<List<MessageEntity>>

    //@Query("SELECT sender, MAX(timestamp) AS timestamp, body, type, id FROM messages GROUP BY senderNormalized ORDER BY timestamp DESC")
    @Query("""
        SELECT id, threadId, sender, senderNormalized, body, 
        MAX(timestamp) AS timestamp, type, read, deliveryStatus, simId, starred, deleted 
        FROM messages 
        GROUP BY senderNormalized 
        ORDER BY timestamp DESC""")
    fun getConversationList(): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE senderNormalized = :senderNormalized
        ORDER BY timestamp ASC""")
    fun getMessagesPage(senderNormalized: String): PagingSource<Int, MessageEntity>
    
    @Query("UPDATE messages SET read = 1 WHERE senderNormalized = :senderNormalized")
    suspend fun markAsRead(senderNormalized: String)

    @Query("DELETE FROM messages WHERE senderNormalized = :senderNormalized")
    suspend fun deleteMessages(senderNormalized: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("UPDATE messages SET starred = :isStarred WHERE id = :id")
    suspend fun starMessage(id: Long, isStarred: Boolean)

    @Query("UPDATE messages SET deliveryStatus = :status WHERE id = :id")
    suspend fun updateDeliveryStatus(id: Long, status: DeliveryStatus)

    @Query("SELECT * FROM messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE deleted = 0 GROUP BY threadId ORDER BY timestamp DESC")
    fun getThreads(): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET read = 1 WHERE threadId = :threadId")
    suspend fun markThreadAsRead(threadId: Long)

    @Query("DELETE FROM messages WHERE deleted = 1")
    suspend fun purgeDeletedMessages()
}
