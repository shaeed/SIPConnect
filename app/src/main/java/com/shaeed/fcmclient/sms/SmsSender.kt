package com.shaeed.fcmclient.sms

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.shaeed.fcmclient.data.SmsRepository
import com.shaeed.fcmclient.network.RestApiClient

object SmsSender {
    fun send(context: Context, to: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(to, null, message, null, null)

        // RestApiClient.sendToBackend(to, message)

        SmsRepository.insertOutgoingMessage(context, to, message, System.currentTimeMillis())
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
