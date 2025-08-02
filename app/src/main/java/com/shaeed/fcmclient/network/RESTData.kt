package com.shaeed.fcmclient.network

data class RegisterDevice(val device_id: String, val username: String, val fcm_token: String)
data class SmsAlert(val username: String, val phone_number: String, val body: String, val device_id: String)

data class PostResponse(val id: Int, val title: String, val body: String)

data class RestartSip(val device_id: String, val username: String)
