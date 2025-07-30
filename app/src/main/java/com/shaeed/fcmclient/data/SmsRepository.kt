package com.shaeed.fcmclient.data

import android.content.ContentValues
import android.content.Context
import androidx.core.net.toUri
import com.shaeed.fcmclient.util.ContactHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SmsRepository {
    fun insertGsmMessage(context: Context, sender: String, body: String, timestamp: Long, simId: Int?) {
        insertIntoSystemInbox(context, sender, body, timestamp)
        insertRoomMessage(context, sender, body, timestamp, MessageType.INCOMING_GSM, simId)
    }

    fun insertFirebaseMessage(context: Context, sender: String, body: String, timestamp: Long) {
        insertIntoSystemInbox(context, sender, body, timestamp)
        insertRoomMessage(context, sender, body, timestamp, MessageType.INCOMING_FIREBASE, 10)
    }

    fun insertOutgoingMessage(context: Context, to: String, body: String, timestamp: Long) {
        insertIntoSystemSent(context, to, body, timestamp)
        insertRoomMessage(context, to, body, timestamp, MessageType.OUTGOING, 10)
    }

    private fun insertIntoSystemInbox(context: Context, sender: String, body: String, timestamp: Long) {
        val values = ContentValues().apply {
            put("address", sender)
            put("body", body)
            put("read", 0)
            put("date", timestamp)
        }
        // context.contentResolver.insert("content://sms/inbox".toUri(), values)
    }

    private fun insertIntoSystemSent(context: Context, to: String, body: String, timestamp: Long) {
        val values = ContentValues().apply {
            put("address", to)
            put("body", body)
            put("read", 1)
            put("date", timestamp)
            put("type", 2) // 2 = SENT
        }
        // context.contentResolver.insert("content://sms/sent".toUri(), values)
    }

    private fun insertRoomMessage(context: Context, sender: String, body: String, timestamp: Long, type: MessageType, simId: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            val senderNormalized = ContactHelper.normalizeNumber(sender)
            AppDatabase.getDatabase(context).messageDao().insert(
                MessageEntity(
                    sender = sender,
                    senderNormalized = senderNormalized,
                    threadId = 1, // Fake value
                    body = body,
                    timestamp = timestamp,
                    type = type,
                    simId = simId
                )
            )
        }
    }
}
