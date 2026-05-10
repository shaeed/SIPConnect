package com.shaeed.fcmclient.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMessageRepository : MessageRepository {
    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())

    val sentMessages = mutableListOf<Pair<String, String>>()
    val readSenders = mutableListOf<String>()
    val deletedSenders = mutableListOf<String>()
    val deletedIds = mutableListOf<Long>()

    fun emit(messages: List<MessageEntity>) {
        _messages.value = messages
    }

    override fun getConversationList(): Flow<List<MessageEntity>> =
        _messages.map { list ->
            list.groupBy { it.senderNormalized }
                .map { (_, msgs) -> msgs.maxBy { it.timestamp } }
                .sortedByDescending { it.timestamp }
        }

    override fun getMessagesForSender(senderNormalized: String): Flow<List<MessageEntity>> =
        _messages.map { list -> list.filter { it.senderNormalized == senderNormalized } }

    override fun getMessagesPage(senderNormalized: String): PagingSource<Int, MessageEntity> =
        object : PagingSource<Int, MessageEntity>() {
            override fun getRefreshKey(state: PagingState<Int, MessageEntity>) = null
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MessageEntity> =
                LoadResult.Page(
                    data = _messages.value.filter { it.senderNormalized == senderNormalized },
                    prevKey = null,
                    nextKey = null
                )
        }

    override suspend fun markAsRead(senderNormalized: String) {
        readSenders += senderNormalized
        _messages.value = _messages.value.map { msg ->
            if (msg.senderNormalized == senderNormalized) msg.copy(read = true) else msg
        }
    }

    override suspend fun deleteMessages(senderNormalized: String) {
        deletedSenders += senderNormalized
        _messages.value = _messages.value.filter { it.senderNormalized != senderNormalized }
    }

    override suspend fun deleteMessage(id: Long) {
        deletedIds += id
        _messages.value = _messages.value.filter { it.id != id }
    }

    override suspend fun sendMessage(to: String, body: String) {
        sentMessages += Pair(to, body)
    }
}
