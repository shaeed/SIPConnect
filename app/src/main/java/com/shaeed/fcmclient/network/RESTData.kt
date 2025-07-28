package com.shaeed.fcmclient.network

data class PostRequest(val device_id: String, val sip_user: String, val fcm_token: String)

data class PostResponse(val id: Int, val title: String, val body: String)
