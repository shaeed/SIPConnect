package com.shaeed.fcmclient.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager
import android.util.Log
import android.net.Uri

class HeadlessSmsSendService : Service() {

    companion object {
        private const val TAG = "HeadlessSmsSendService"
        private const val ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with intent: $intent")

        if (intent != null && intent.action == ACTION_RESPOND_VIA_MESSAGE) {
            val uri: Uri? = intent.data
            val extras = intent.extras

            val messageBody: String? = extras?.getCharSequence(Intent.EXTRA_TEXT)?.toString()
            val phoneNumber: String? = uri?.schemeSpecificPart

            Log.d(TAG, "Quick reply received for: $phoneNumber with message: $messageBody")

            if (!phoneNumber.isNullOrBlank() && !messageBody.isNullOrBlank()) {
                try {
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(phoneNumber, null, messageBody, null, null)
                    Log.d(TAG, "SMS sent successfully to $phoneNumber")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to send SMS: ${e.message}", e)
                }
            } else {
                Log.w(TAG, "Phone number or message is null/blank. Skipping.")
            }
        } else {
            Log.d(TAG, "Intent action is not RESPOND_VIA_MESSAGE. Ignored.")
        }

        stopSelf()
        return START_NOT_STICKY
    }
}
