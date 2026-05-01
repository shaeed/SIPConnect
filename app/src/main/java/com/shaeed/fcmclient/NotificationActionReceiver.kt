package com.shaeed.fcmclient

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shaeed.fcmclient.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val dao = AppDatabase.getDatabase(context).messageDao()

        CoroutineScope(Dispatchers.IO).launch {
            when (intent.action) {
                ACTION_DELETE -> {
                    val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
                    if (messageId != -1L) dao.deleteMessage(messageId)
                }
                ACTION_MARK_READ -> {
                    val senderNormalized = intent.getStringExtra(EXTRA_SENDER_NORMALIZED) ?: return@launch
                    dao.markAsRead(senderNormalized)
                }
            }
        }

        if (notificationId != -1) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationId)
        }
    }

    companion object {
        const val ACTION_DELETE = "com.shaeed.fcmclient.ACTION_SMS_DELETE"
        const val ACTION_MARK_READ = "com.shaeed.fcmclient.ACTION_SMS_MARK_READ"
        const val EXTRA_SENDER_NORMALIZED = "senderNormalized"
        const val EXTRA_NOTIFICATION_ID = "notificationId"
        const val EXTRA_MESSAGE_ID = "messageId"
    }
}
