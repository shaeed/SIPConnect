package com.shaeed.fcmclient

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.data.CallLog
import com.shaeed.fcmclient.data.SmsLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MyFirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }

        val data = remoteMessage.data
        Log.d("Notification", data.toString())
        when (data["type"]) {
            "call" -> showIncomingCallNotification(applicationContext, data["phone_number"])
            "missed-call" -> showMissedCallNotification(data)
            "sms" -> showIncomingSmsNotification(data)
            else -> Log.w("FCM", "Unknown notification type: ${data["type"]}")
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "default_channel_id")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with your icon
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        // Optionally upload to your server
    }

    private fun showIncomingCallNotification(context: Context, phoneNumber: String?) {
        // Delay start by 1 second to give the time after notification to app to come to foreground
        Handler(Looper.getMainLooper()).postDelayed({
            val serviceIntent = Intent(context, CallForegroundService::class.java)
            serviceIntent.putExtra("phone_number", phoneNumber)
            ContextCompat.startForegroundService(context, serviceIntent)
        }, 1000)
    }

    private fun showMissedCallNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        addCallerToDb(from, "missed")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "callHistory")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "default_channel_id")
            .setSmallIcon(android.R.drawable.sym_call_missed)
            .setContentTitle("Missed call")
            .setContentText("From $from")
            .setStyle(NotificationCompat.BigTextStyle().bigText("From $from"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun showIncomingSmsNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        val body = data["body"] ?: ""
        addSmsToDb(from, body)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "smsHistory")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "sms_channel")
            .setSmallIcon(android.R.drawable.sym_action_email)
            .setContentTitle("SMS from $from")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notification)
    }

    fun addSmsToDb(from: String, body: String){
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
}
