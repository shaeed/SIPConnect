package com.shaeed.fcmclient.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @POST()
    fun registerDeviceOnServer(@Url url: String, @Body request: RegisterDevice): Call<PostResponse>

    @POST()
    fun smsAlert(@Url url: String, @Body request: SmsAlert): Call<PostResponse>

    @POST()
    fun sendGsmSms(@Url url: String, @Body request: SmsAlert): Call<PostResponse>

    @POST()
    fun restartSip(@Url url: String, @Body request: RestartSip): Call<PostResponse>
}