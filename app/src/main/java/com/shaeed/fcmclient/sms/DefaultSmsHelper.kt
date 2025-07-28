package com.shaeed.fcmclient.sms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

object DefaultSmsHelper {
    fun isDefaultSmsApp(context: Context): Boolean {
        Log.d("DefaultSmsHelper", "Checking if default sms")
        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }

    fun requestDefaultSmsApp(activity: Activity) {
        Log.d("DefaultSmsHelper", "Requesting default sms")
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
        activity.startActivity(intent)
    }
}
