package com.shaeed.fcmclient.network

import android.content.Context
import android.os.Build
import android.util.Log
import com.shaeed.fcmclient.data.PrefKeys
import com.shaeed.fcmclient.data.SharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    data class CommonRequestParams(
        val deviceId: String,
        val server: String,
        val username: String
    )

    private fun getCommonParams(context: Context): CommonRequestParams {
        val deviceId = "${Build.MANUFACTURER} ${Build.MODEL}"
        val server = SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS)
        val username = SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER)
        return CommonRequestParams(deviceId, server, username)
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://example.com/") // required by Retrofit, but ignored when using @Url inside ApiService.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun <T> handleRetrofitCall(call: Call<T>, onResult: (Any?) -> Unit) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val result = if (response.isSuccessful) {
                    response.body()
                } else {
                    val errorMsg = response.errorBody()?.string().orEmpty()
                    "Error code: ${response.code()}. Error: $errorMsg"
                }
                onResult(result)
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val result = "Failure: ${t.message}"
                Log.e("RetrofitClient", result)
                onResult(result)
            }
        })
    }

    fun uploadFCMToServer(context: Context, fcmToken: String, onResult: (String) -> Unit) {
        val (deviceId, server, username) = getCommonParams(context)
        val request = RegisterDevice(deviceId, username, fcmToken)
        val url = "http://$server/sip/client/register"
        Log.d("RetrofitClient", "URL: $url")

        handleRetrofitCall(apiService.registerDeviceOnServer(url, request)) { result ->
            onResult( if(result is PostResponse) result.message else result as String )
        }
    }

    fun sendSmsAlert(context: Context, phoneNumber: String, body: String, onResult: (String) -> Unit) {
        val (deviceId, server, username) = getCommonParams(context)
        val request = SmsAlert(username, phoneNumber, body, deviceId)
        val url = "http://$server/sip/alert/sms"
        Log.d("RetrofitClient", "URL: $url")

        handleRetrofitCall(apiService.smsAlert(url, request)) { result ->
            onResult( if(result is PostResponse) result.message else result as String )
        }
    }

    fun sendGsmSms(context: Context, phoneNumber: String, body: String, onResult: (String) -> Unit) {
        val (deviceId, server, username) = getCommonParams(context)
        val request = SmsAlert(username, phoneNumber, body, deviceId)
        val url = "http://$server/gsm/sms"
        Log.d("RetrofitClient", "URL: $url")

        handleRetrofitCall(apiService.sendGsmSms(url, request)) { result ->
            onResult( if(result is PostResponse) result.message else result as String )
        }
    }

    fun restartSip(context: Context, onResult: (String) -> Unit) {
        val (deviceId, server, username) = getCommonParams(context)
        val request = RestartSip(deviceId, username)
        val url = "http://$server/sip/restart"
        Log.d("RetrofitClient", "URL: $url")

        handleRetrofitCall(apiService.restartSip(url, request)) { result ->
            onResult( if(result is PostResponse) result.message else result as String )
        }
    }

    fun getTokenFromServer(context: Context, onResult: (String?) -> Unit) {
        val (deviceId, server, username) = getCommonParams(context)
        val url = "http://$server/sip/client/token"
        Log.d("RetrofitClient", "URL: $url")

        val options = mapOf("username" to username, "device_id" to deviceId)
        handleRetrofitCall(apiService.getTokenFromServer(url, options)) { result ->
            if(result is TokenResponse) onResult(result.fcm_token)
            else onResult(null)
        }
    }
}