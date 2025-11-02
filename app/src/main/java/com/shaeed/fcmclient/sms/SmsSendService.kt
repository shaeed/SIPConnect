package com.shaeed.fcmclient.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val extras = intent?.extras
        val phoneNumber = extras?.getString("destAddr")
        val messageBody = extras?.getString("text")

        if (!phoneNumber.isNullOrBlank() && !messageBody.isNullOrBlank()) {
            Log.d("SmsSendService", "Sending SMS: $phoneNumber -> $messageBody")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    SmsSender.send(applicationContext, phoneNumber, messageBody)
                    Log.d("SmsSendService", "Message sent successfully")
                } catch (e: Exception) {
                    Log.e("SmsSendService", "Send failed", e)
                }
            }
        }

        stopSelf()
        return START_NOT_STICKY
    }
}
