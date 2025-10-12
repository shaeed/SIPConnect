package com.shaeed.fcmclient.data

import android.content.Context
import androidx.core.content.edit

object PrefKeys {
    const val IP_ADDRESS = "ip_address"
    const val LAST_FCM_TOKEN = "fcm_token"
    const val SIP_SERVER_USER = "sip_server_user"
    const val SIP_SERVER_PASS = "sip_server_pass"
    const val SIP_SERVER_USER2 = "sip_server_user2"
    const val SIP_SERVER_PASS2 = "sip_server_pass2"
    const val REGISTRATION_STATUS = "registration_status_on_server"
    const val APP_MODE = "app_mode"
}

object PerfValues {
    const val YES = "yes"
    const val NO = "no"
}

object AppMode {
    const val SERVER = "server"
    const val CLIENT = "client"
    const val SMS_MANAGER = "sms_manager"
    const val NORMAL = "normal"
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