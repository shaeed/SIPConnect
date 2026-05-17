package com.shaeed.fcmclient.data

data class GroupedCallLog(
    val normalizedNumber: String,
    val phoneNumber: String,
    val count: Int,
    val latestTimestamp: Long,
    val latestStatus: String,
    val calls: List<CallLog>
)
