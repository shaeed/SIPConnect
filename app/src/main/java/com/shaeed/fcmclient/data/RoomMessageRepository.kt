package com.shaeed.fcmclient.data

import android.content.Context
import androidx.paging.PagingSource
import com.shaeed.fcmclient.sms.SmsSender
import kotlinx.coroutines.flow.Flow

class RoomMessageRepository(
    private val dao: MessageDao,
    private val context: Context
) : MessageRepository {
    override fun getConversationList(): Flow<List<MessageEntity>> = dao.getConversationList()
    override fun getMessagesForSender(senderNormalized: String): Flow<List<MessageEntity>> = dao.getMessagesForSender(senderNormalized)
    override fun getMessagesPage(senderNormalized: String): PagingSource<Int, MessageEntity> = dao.getMessagesPage(senderNormalized)
    override suspend fun markAsRead(senderNormalized: String) = dao.markAsRead(senderNormalized)
    override suspend fun deleteMessages(senderNormalized: String) = dao.deleteMessages(senderNormalized)
    override suspend fun deleteMessage(id: Long) = dao.deleteMessage(id)
    override suspend fun sendMessage(to: String, body: String) = SmsSender.send(context, to, body)
}
