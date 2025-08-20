package com.shaeed.fcmclient.sms

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.shaeed.fcmclient.data.DeliveryStatus
import com.shaeed.fcmclient.data.SmsRepository
import com.shaeed.fcmclient.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsSender {
    suspend fun send(context: Context, to: String, body: String) {
        // val smsManager = SmsManager.getDefault()
        // smsManager.sendTextMessage(to, null, message, null, null)

        var errored = false
        val messageID = SmsRepository.insertOutgoingMessage(context, to, body, System.currentTimeMillis())
        var response = ""
        try {
            val result = RetrofitClient.sendGsmSms(context, to, body)
            if (!result.isSuccessful) {
                errored = true
                val errorMsg = result.errorBody()?.string().orEmpty()
                response = "Error code: ${result.code()}. Error: $errorMsg"
            } else {
                response = result.body()?.message!!
            }
        } catch (e: Exception) {
            errored = true
            response =  "Failure: ${e.message}"
        }
        Log.d("SmsSender", response)
        if (errored){
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failure: $response", Toast.LENGTH_LONG).show()
            }
        }

        val status = if (errored) DeliveryStatus.FAILED else DeliveryStatus.SENT
        SmsRepository.updateDeliveryStatus(context, messageID, status)
    }

    fun sendSms(
        context: Context,
        to: String,
        body: String,
        subscriptionId: Int? = null  // Pass SIM slot if needed
    ) {
        val smsManager = if (subscriptionId != null) {
            SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
        } else {
            SmsManager.getDefault()
        }

        val sentIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent("SMS_SENT"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent("SMS_DELIVERED"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        smsManager.sendTextMessage(to, null, body, sentIntent, deliveredIntent)

        Log.d("SmsSender", "Sent SMS to $to via SIM: $subscriptionId")
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getAvailableSimSlots(context: Context): List<SubscriptionInfo> {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        return subscriptionManager.activeSubscriptionInfoList ?: emptyList()
    }
}
