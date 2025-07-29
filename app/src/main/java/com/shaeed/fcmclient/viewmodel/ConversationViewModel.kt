package com.shaeed.fcmclient.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.sms.SmsSender
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConversationViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getDatabase(app).messageDao()

    fun getMessages(senderNormalized: String) = dao.getMessagesForSender(senderNormalized)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(to: String, body: String) {
        SmsSender.send(getApplication(), to, body)
    }

    fun markAsRead(senderNormalized: String) {
        viewModelScope.launch { dao.markAsRead(senderNormalized) }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { dao.deleteMessage(id) }
    }
}
