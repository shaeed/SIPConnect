package com.shaeed.fcmclient.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.data.RoomMessageRepository

class ConversationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ConversationViewModel(
            RoomMessageRepository(AppDatabase.getDatabase(context).messageDao(), context.applicationContext)
        ) as T
    }
}
