package com.shaeed.fcmclient

import com.shaeed.fcmclient.data.PostRequest
import com.shaeed.fcmclient.data.PostResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface ApiService {
    @POST()
    fun createPost(@Url url: String, @Body request: PostRequest): Call<PostResponse>
}
