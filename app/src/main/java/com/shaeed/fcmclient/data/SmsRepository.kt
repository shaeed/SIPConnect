package com.shaeed.fcmclient.data

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.provider.Telephony
import androidx.core.net.toUri
import com.shaeed.fcmclient.GlobalConfig
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

    private fun insertIntoSystemInboxOld(context: Context, sender: String, body: String, timestamp: Long) {
        if(GlobalConfig.compileMode != AppMode.SMS_MANAGER) {
            return
        }

        val values = ContentValues().apply {
            put("address", sender)
            put("body", body)
            put("read", 0)
            put("date", timestamp)
        }
        context.contentResolver.insert("content://sms/inbox".toUri(), values)
    }

    fun insertIntoSystemInbox(
        context: Context,
        address: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        subscriptionId: Int? = null
    ): Boolean {
        try {
            val operations = arrayListOf<ContentProviderOperation>()
            val inboxUri = Telephony.Sms.Inbox.CONTENT_URI

            val builder = ContentProviderOperation.newInsert(inboxUri)
                .withValue(Telephony.Sms.ADDRESS, address)
                .withValue(Telephony.Sms.BODY, body)
                .withValue(Telephony.Sms.DATE, timestamp)
                .withValue(Telephony.Sms.READ, 0)
                .withValue(Telephony.Sms.SEEN, 0)
                .withValue(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)

            if (subscriptionId != null) {
                builder.withValue("sub_id", subscriptionId)
            }

            operations.add(builder.build())
            context.contentResolver.applyBatch("sms", operations)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun insertIntoSystemSentOld(context: Context, to: String, body: String, timestamp: Long) {
        if(GlobalConfig.compileMode != AppMode.SMS_MANAGER) {
            return
        }

        val values = ContentValues().apply {
            put("address", to)
            put("body", body)
            put("read", 1)
            put("date", timestamp)
            put("type", 2) // 2 = SENT
        }
        context.contentResolver.insert("content://sms/sent".toUri(), values)
    }

    fun insertIntoSystemSent(
        context: Context,
        address: String,
        body: String,
        timestamp: Long = System.currentTimeMillis(),
        subscriptionId: Int? = null
    ): Boolean {
        return try {
            val operations = arrayListOf<ContentProviderOperation>()
            val sentUri = Telephony.Sms.Sent.CONTENT_URI

            val builder = ContentProviderOperation.newInsert(sentUri)
                .withValue(Telephony.Sms.ADDRESS, address)
                .withValue(Telephony.Sms.BODY, body)
                .withValue(Telephony.Sms.DATE, timestamp)
                .withValue(Telephony.Sms.READ, 1)
                .withValue(Telephony.Sms.SEEN, 1)
                .withValue(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)

            if (subscriptionId != null) {
                builder.withValue("sub_id", subscriptionId)
            }

            operations.add(builder.build())
            context.contentResolver.applyBatch("sms", operations)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
