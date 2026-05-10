package com.shaeed.fcmclient.myui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shaeed.fcmclient.data.addCallerToDb
import com.shaeed.fcmclient.util.UtilFunctions

class IncomingCallActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        super.onCreate(savedInstanceState)
        Log.d("IncomingCallActivity", "Incoming call notification tapped")

        // Get data from the Intent
        val from = intent.getStringExtra("from") ?: "Unknown"
        val timestampStr = intent.getStringExtra("timestamp") ?: "unknown"
        val timestamp = UtilFunctions.isoToMillis(timestampStr)
        addCallerToDb(from, "Incoming", timestamp, applicationContext)

        // launch ZoiPer
        val packageName = "com.zoiper.android.app"
        val zoiperIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (zoiperIntent != null) {
            startActivity(zoiperIntent)
        } else {
            Toast.makeText(this, "ZoiPer not installed.", Toast.LENGTH_LONG).show()
        }

        finish()
    }
}
