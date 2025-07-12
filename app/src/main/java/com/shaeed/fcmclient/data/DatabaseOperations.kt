package com.shaeed.fcmclient.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun addSmsToDb(from: String, body: String, applicationContext: Context){
    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(applicationContext)
        val smsLog = SmsLog(
            from = from,
            body = body,
            timestamp = System.currentTimeMillis(),
        )
        db.smsLogDao().insert(smsLog)
    }
}

fun addCallerToDb(phoneNumber: String, status: String, applicationContext: Context){
    CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(applicationContext)
        val callLog = CallLog(
            phoneNumber = phoneNumber,
            timestamp = System.currentTimeMillis(),
            status = status // missed or "received"
        )
        db.callLogDao().insert(callLog)
    }
}