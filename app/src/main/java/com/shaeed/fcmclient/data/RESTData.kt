package com.shaeed.fcmclient.data

data class PostRequest(val device_id: String, val user_name: String, val user_pass: String, val fcm_token: String)

data class PostResponse(val id: Int, val title: String, val body: String)
