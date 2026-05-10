package com.shaeed.fcmclient.data

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getConversationList(): Flow<List<MessageEntity>>
    fun getMessagesForSender(senderNormalized: String): Flow<List<MessageEntity>>
    fun getMessagesPage(senderNormalized: String): PagingSource<Int, MessageEntity>
    suspend fun markAsRead(senderNormalized: String)
    suspend fun deleteMessages(senderNormalized: String)
    suspend fun deleteMessage(id: Long)
    suspend fun sendMessage(to: String, body: String)
}
