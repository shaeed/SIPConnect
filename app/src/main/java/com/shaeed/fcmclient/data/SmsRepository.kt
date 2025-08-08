package com.shaeed.fcmclient.data

import android.content.ContentValues
import android.content.Context
import com.shaeed.fcmclient.util.ContactHelper

object SmsRepository {
    suspend fun insertGsmMessage(context: Context, sender: String, body: String, timestamp: Long, simId: Int?): Long {
        insertIntoSystemInbox(context, sender, body, timestamp)
        val messageId = insertRoomMessage(context, sender, body, timestamp, MessageType.INCOMING_GSM, simId)
        return messageId
    }

    suspend fun insertFirebaseMessage(context: Context, sender: String, body: String, timestamp: Long): Long {
        insertIntoSystemInbox(context, sender, body, timestamp)
        val messageId = insertRoomMessage(context, sender, body, timestamp, MessageType.INCOMING_FIREBASE, 10)
        return messageId
    }

    suspend fun insertOutgoingMessage(context: Context, to: String, body: String, timestamp: Long): Long {
        insertIntoSystemSent(context, to, body, timestamp)
        val messageId = insertRoomMessage(context, to, body, timestamp,MessageType.OUTGOING, 10)
        return messageId
    }

    private suspend fun insertIntoSystemInbox(context: Context, sender: String, body: String, timestamp: Long) {
        val values = ContentValues().apply {
            put("address", sender)
            put("body", body)
            put("read", 0)
            put("date", timestamp)
        }
        // context.contentResolver.insert("content://sms/inbox".toUri(), values)
    }

    private suspend fun insertIntoSystemSent(context: Context, to: String, body: String, timestamp: Long) {
        val values = ContentValues().apply {
            put("address", to)
            put("body", body)
            put("read", 1)
            put("date", timestamp)
            put("type", 2) // 2 = SENT
        }
        // context.contentResolver.insert("content://sms/sent".toUri(), values)
    }

    private suspend fun insertRoomMessage(
        context: Context, sender: String, body: String,
        timestamp: Long, type: MessageType, simId: Int?): Long {
        val senderNormalized = ContactHelper.normalizeNumber(sender)
        val messageId = AppDatabase.getDatabase(context).messageDao().insert(
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
        return messageId
    }

    suspend fun updateDeliveryStatus(context: Context, id: Long, status: DeliveryStatus) {
         AppDatabase.getDatabase(context).messageDao().updateDeliveryStatus(id, status)
    }
}
