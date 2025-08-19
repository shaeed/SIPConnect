package com.shaeed.fcmclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.data.MessageEntity
import com.shaeed.fcmclient.sms.SmsSender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).messageDao()

    fun getMessages(senderNormalized: String) = dao.getMessagesForSender(senderNormalized)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val pagedMessagesMap = mutableMapOf<String, Flow<PagingData<MessageEntity>>>()

    fun getPagedMessages(senderNormalized: String): Flow<PagingData<MessageEntity>> {
        return pagedMessagesMap.getOrPut(senderNormalized) {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { dao.getMessagesPage(senderNormalized) }
            ).flow.cachedIn(viewModelScope)
        }
    }

    suspend fun sendMessage(to: String, body: String) {
        SmsSender.send(getApplication(), to, body)
    }

    fun markAsRead(senderNormalized: String) {
        viewModelScope.launch { dao.markAsRead(senderNormalized) }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { dao.deleteMessage(id) }
    }
}
