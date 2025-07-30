package com.shaeed.fcmclient.network

data class RegisterDevice(val device_id: String, val sip_user: String, val fcm_token: String)
data class SmsAlert(val sip_user: String, val phone_number: String, val body: String, val device_id: String)

data class PostResponse(val id: Int, val title: String, val body: String)
