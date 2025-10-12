package com.shaeed.fcmclient.sms

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.shaeed.fcmclient.data.AppMode
import com.shaeed.fcmclient.data.PrefKeys
import com.shaeed.fcmclient.data.SharedPreferences
import com.shaeed.fcmclient.data.SmsRepository
import com.shaeed.fcmclient.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            val bundle = intent.extras
            // val pdus = bundle?.get("pdus") as? Array<*>
            val pdus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle?.getSerializable("pdus", Array<ByteArray>::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle?.get("pdus") as? Array<*>
            }

            val format = bundle?.getString("format")
            val subscriptionId = bundle?.getInt("subscription", -1)
            Log.d("SmsReceiver", "SMS received on subscriptionId: $subscriptionId")

            pdus?.forEach {
                val sms = SmsMessage.createFromPdu(it as ByteArray, format)
                val sender = sms.displayOriginatingAddress
                val body = sms.displayMessageBody
                val timestamp = sms.timestampMillis

                val slot = getSimSlot(context, subscriptionId)
                Log.d("SmsReceiver", "subscriptionId: $subscriptionId, Slot: $slot. From: $sender Message: $body")

                CoroutineScope(Dispatchers.IO).launch {
                    if(SharedPreferences.getKeyValue(context, PrefKeys.APP_MODE) == AppMode.SERVER) {
                        val result = RetrofitClient.sendSmsAlert(context, sender, body)
                        Log.d("SmsReceiver", result)
                    }
                    SmsRepository.insertGsmMessage(context, sender, body, timestamp, subscriptionId)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getSimSlot(context: Context, subscriptionId: Int?): Int{
        if (subscriptionId == null) return -1
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        val subscriptionInfo = subscriptionManager.getActiveSubscriptionInfo(subscriptionId)

        if (subscriptionInfo != null) {
            val carrierName = subscriptionInfo.carrierName
            val slotIndex = subscriptionInfo.simSlotIndex
            Log.d("SmsReceiver", "Carrier: $carrierName, Slot: $slotIndex")
            return slotIndex
        }
        return -1
    }
}
