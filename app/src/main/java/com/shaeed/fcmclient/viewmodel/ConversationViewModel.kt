package com.shaeed.fcmclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.shaeed.fcmclient.data.MessageEntity
import com.shaeed.fcmclient.data.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationViewModel(private val repository: MessageRepository) : ViewModel() {

    fun getMessages(senderNormalized: String) = repository.getMessagesForSender(senderNormalized)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val pagedMessagesMap = mutableMapOf<String, Flow<PagingData<MessageEntity>>>()

    fun getPagedMessages(senderNormalized: String): Flow<PagingData<MessageEntity>> {
        return pagedMessagesMap.getOrPut(senderNormalized) {
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = { repository.getMessagesPage(senderNormalized) }
            ).flow.cachedIn(viewModelScope)
        }
    }

    suspend fun sendMessage(to: String, body: String) {
        repository.sendMessage(to, body)
    }

    fun markAsRead(senderNormalized: String) {
        viewModelScope.launch { repository.markAsRead(senderNormalized) }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { repository.deleteMessage(id) }
    }
}
