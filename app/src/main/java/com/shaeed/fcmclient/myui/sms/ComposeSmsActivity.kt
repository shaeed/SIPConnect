package com.shaeed.fcmclient.myui.sms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shaeed.fcmclient.sms.SmsSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ComposeSmsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        val messageBody = intent.getStringExtra("sms_body")
        val phoneNumber = uri?.schemeSpecificPart

        if (!phoneNumber.isNullOrBlank() && !messageBody.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                SmsSender.send(applicationContext, phoneNumber, messageBody)
            }
        }

        finish()
    }
}
