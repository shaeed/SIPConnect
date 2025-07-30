package com.shaeed.fcmclient.util

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object UtilFunctions {
    fun formatTimestamp(timestamp: Long): String {
        val messageDate = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }

        val today = Calendar.getInstance()
        val sameDay = today.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)

        return if (sameDay) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(Date(timestamp))
        } else {
            val dateTimeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            dateTimeFormat.format(Date(timestamp))
        }
    }

    fun getDeviceId(): String {
        val deviceId = "${Build.MANUFACTURER} ${Build.MODEL}"
        return deviceId
    }
}
