package com.shaeed.fcmclient.myui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.shaeed.fcmclient.data.addCallerToDb

class IncomingCallActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // overridePendingTransition(0, 0)
        Log.d("IncomingCallActivity", "Incoming call notification tapped")

        // Get data from the Intent
        val from = intent.getStringExtra("from") ?: "Unknown"
        addCallerToDb(from, "Incoming", applicationContext)

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
