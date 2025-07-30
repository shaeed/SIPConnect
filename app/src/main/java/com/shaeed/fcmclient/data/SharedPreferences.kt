package com.shaeed.fcmclient.data

import android.content.Context
import androidx.core.content.edit

object PrefKeys {
    const val IP_ADDRESS = "ip_address"
    const val LAST_FCM_TOKEN = "fcm_token"
    const val SIP_SERVER_USER = "sip_server_user"
    const val SIP_SERVER_PASS = "sip_server_pass"
}

object SharedPreferences {
    fun saveKeyValue(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString(key, value)
        }
    }

    fun getKeyValue(context: Context, key: String): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(key, "") ?: ""
    }

}