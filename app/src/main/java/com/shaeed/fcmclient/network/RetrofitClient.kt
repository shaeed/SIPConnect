package com.shaeed.fcmclient.network

import android.content.Context
import android.util.Log
import com.shaeed.fcmclient.data.PrefKeys
import com.shaeed.fcmclient.data.SharedPreferences
import com.shaeed.fcmclient.util.UtilFunctions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://example.com/") // required by Retrofit, but ignored when using @Url inside ApiService.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun uploadFCMToServer(context: Context, fcmToken: String, onResult: (String) -> Unit) {
        val deviceId = UtilFunctions.getDeviceId()
        val server = SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS)
        val username = SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER)

        val request = RegisterDevice(deviceId, username, fcmToken)
        val url = "http://$server/sip/client/register"
        Log.d("RetrofitClient", "URL: $url")

        apiService.registerDeviceOnServer(url, request)
            .enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    val result = if (response.isSuccessful) {
                        "Success! Device registered on server."
                    } else {
                        "Error code: ${response.code()}. ${response.body()}"
                    }
                    onResult(result)
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    val result = "Failure: ${t.message}"
                    Log.e("RetrofitClient", result)
                    onResult(result)
                }
            })
    }

    fun sendSmsAlert(context: Context, phoneNumber: String, body: String, onResult: (String) -> Unit) {
        val deviceId = UtilFunctions.getDeviceId()
        val server = SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS)
        val username = SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER)

        val request = SmsAlert(username, phoneNumber, body, deviceId)
        val url = "http://$server/sip/alert/sms"
        Log.d("RetrofitClient", "URL: $url")

        apiService.smsAlert(url, request)
            .enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    val result = if (response.isSuccessful) {
                        "${response.body()}"
                    } else {
                        "Error code: ${response.code()}. Error: ${response.errorBody()?.string()}"
                    }
                    onResult(result)
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    val result = "Failure: ${t.message}"
                    Log.e("RetrofitClient", result)
                    onResult(result)
                }
            })
    }

    fun sendGsmSms(context: Context, phoneNumber: String, body: String, onResult: (String) -> Unit) {
        val deviceId = UtilFunctions.getDeviceId()
        val server = SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS)
        val username = SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER)

        val request = SmsAlert(username, phoneNumber, body, deviceId)
        val url = "http://$server/gsm/sms"
        Log.d("RetrofitClient", "URL: $url")

        apiService.sendGsmSms(url, request)
            .enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    val result = if (response.isSuccessful) {
                        "${response.body()}"
                    } else {
                        "Error code: ${response.code()}. Error: ${response.errorBody()?.string()}"
                    }
                    onResult(result)
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    val result = "Failure: ${t.message}"
                    Log.e("RetrofitClient", result)
                    onResult(result)
                }
            })
    }

    fun restartSip(context: Context, onResult: (String) -> Unit) {
        val deviceId = UtilFunctions.getDeviceId()
        val server = SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS)
        val username = SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER)

        val request = RestartSip(deviceId, username)
        val url = "http://$server/sip/restart"
        Log.d("RetrofitClient", "URL: $url")

        apiService.restartSip(url, request)
            .enqueue(object : Callback<PostResponse> {
                override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                    val result = if (response.isSuccessful) {
                        "${response.body()}"
                    } else {
                        "Error code: ${response.code()}. Error: ${response.errorBody()?.string()}"
                    }
                    onResult(result)
                }

                override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                    val result = "Failure: ${t.message}"
                    Log.e("RetrofitClient", result)
                    onResult(result)
                }
            })
    }
}