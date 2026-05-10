package com.shaeed.fcmclient.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.data.RoomCallLogRepository

class CallViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallViewModel(
            RoomCallLogRepository(AppDatabase.getDatabase(context).callLogDao())
        ) as T
    }
}
