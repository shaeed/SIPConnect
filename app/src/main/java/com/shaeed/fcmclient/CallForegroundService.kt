package com.shaeed.fcmclient

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.data.CallLog
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CallForegroundService : Service(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra("phone_number") ?: "Unknown"
        Log.d("CallForegroundService", "Incoming call from: $phoneNumber")

        val packageName = "com.zoiper.android.app"
        val zoiperIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (zoiperIntent == null) {
            Log.e("IncomingCallActivity", "Zoiper not installed.")
            Toast.makeText(this, "Zoiper not installed.", Toast.LENGTH_SHORT).show()
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, zoiperIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, "incoming_call_channel")
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle("Incoming Call")
            .setContentText("From $phoneNumber. Tap to open ZoiPer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()
        Log.d("CallForegroundService", "1")
        startForeground(1, notification)
        addCallerToDb(phoneNumber, "Received")

        // Stop service after a few seconds or when call ends
        launch {
            delay(50_000L) // 50 seconds
            val missedCallNotification = NotificationCompat.Builder(this@CallForegroundService, "incoming_call_channel")
                .setSmallIcon(android.R.drawable.sym_call_missed)
                .setContentTitle("Missed Call")
                .setContentText("from $phoneNumber")
                .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, missedCallNotification) // ← same ID = update
            addCallerToDb(phoneNumber, "Missed")

            stopForeground(false)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    fun addCallerToDb(phoneNumber: String, status: String){
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

    override fun onBind(intent: Intent?): IBinder? = null
}