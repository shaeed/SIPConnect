package com.shaeed.fcmclient.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadlessSmsSendService : Service() {
    companion object {
        private const val TAG = "HeadlessSmsSendService"
        private const val ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == ACTION_RESPOND_VIA_MESSAGE) {
            val uri: Uri? = intent.data
            val extras = intent.extras

            val messageBody: String? = extras?.getCharSequence(Intent.EXTRA_TEXT)?.toString()
            val phoneNumber: String? = uri?.schemeSpecificPart

            Log.d(TAG, "Quick reply received for: $phoneNumber with message: $messageBody")

            if (!phoneNumber.isNullOrBlank() && !messageBody.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val context = this@HeadlessSmsSendService
                    SmsSender.send(context, phoneNumber, messageBody)
                }

                Log.d(TAG, "SMS sent successfully to $phoneNumber")
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }
}
