package com.shaeed.fcmclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.fcmclient.data.MessageRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InboxViewModel(private val repository: MessageRepository) : ViewModel() {

    val conversations = repository.getConversationList().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun deleteMessages(senderNormalized: String) {
        viewModelScope.launch {
            repository.deleteMessages(senderNormalized)
        }
    }
}
