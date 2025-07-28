package com.shaeed.fcmclient.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.shaeed.fcmclient.data.SmsRepository

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as? Array<*>
            val format = bundle?.getString("format")
            pdus?.forEach {
                val sms = SmsMessage.createFromPdu(it as ByteArray, format)
                val sender = sms.displayOriginatingAddress
                val body = sms.displayMessageBody
                val timestamp = sms.timestampMillis

                SmsRepository.insertGsmMessage(context, sender, body, timestamp)
            }
        }
    }
}
