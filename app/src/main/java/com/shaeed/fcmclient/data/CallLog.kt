package com.shaeed.fcmclient.data

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "call_log")
data class CallLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val timestamp: Long,
    val status: String // "Missed", "Incoming", "Outgoing" etc.
)
