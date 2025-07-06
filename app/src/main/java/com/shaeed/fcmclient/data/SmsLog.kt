package com.shaeed.fcmclient.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_log")
data class SmsLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val from: String,
    val body: String,
    val timestamp: Long
)
