package com.shaeed.fcmclient.network

data class RegisterDevice(val device_id: String, val username: String, val fcm_token: String)
data class SmsAlert(val username: String, val phone_number: String, val body: String, val device_id: String)

data class PostResponse(val message: String)

data class RestartSip(val device_id: String, val username: String)

data class TokenResponse(val fcm_token: String)

data class ServerSmsLog(
    val id: Int,
    val user: String,
    val number: String,
    val message: String,
    val sms_type: String,
    val timestamp: String
)

data class ServerCallLog(
    val id: Int,
    val user: String,
    val number: String,
    val timestamp: String
)

data class ServerSmsLogsResponse(val data: List<ServerSmsLog>)
data class ServerCallLogsResponse(val data: List<ServerCallLog>)
