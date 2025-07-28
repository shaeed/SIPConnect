package com.shaeed.fcmclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long,
    val sender: String,
    val senderNormalized: String, // Normalized number
    val body: String,
    val timestamp: Long,
    val type: MessageType,
    val read: Boolean = false,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
    val simId: Int? = null,
    val starred: Boolean = false,
    val deleted: Boolean = false // Lazy delete
)

enum class MessageType {
    INCOMING_GSM, INCOMING_FIREBASE, OUTGOING
}

enum class DeliveryStatus {
    PENDING, SENT, DELIVERED, FAILED
}
