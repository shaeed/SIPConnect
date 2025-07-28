package com.shaeed.fcmclient.sms

import android.content.Context
import android.telephony.SmsManager
import com.shaeed.fcmclient.data.SmsRepository
import com.shaeed.fcmclient.network.RestApiClient

object SmsSender {
    fun send(context: Context, to: String, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(to, null, message, null, null)

        // RestApiClient.sendToBackend(to, message)

        SmsRepository.insertOutgoingMessage(context, to, message, System.currentTimeMillis())
    }
}
