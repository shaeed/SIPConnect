package com.shaeed.fcmclient.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {
    @POST()
    suspend fun registerDeviceOnServer(@Url url: String, @Body request: RegisterDevice): Response<PostResponse>

    @POST()
    suspend fun smsAlert(@Url url: String, @Body request: SmsAlert): Response<PostResponse>

    @POST()
    suspend fun sendGsmSms(@Url url: String, @Body request: SmsAlert): Response<PostResponse>

    @POST()
    suspend fun restartSip(@Url url: String, @Body request: RestartSip): Response<PostResponse>

    @GET()
    suspend fun getTokenFromServer(@Url url: String, @QueryMap options: Map<String, String>): Response<TokenResponse>
}
