package com.shaeed.fcmclient

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shaeed.fcmclient.data.SmsRepository
import com.shaeed.fcmclient.data.addCallerToDb
import com.shaeed.fcmclient.data.addSmsToDb
import com.shaeed.fcmclient.myui.IncomingCallActivity
import com.shaeed.fcmclient.util.ContactHelper
import kotlinx.coroutines.runBlocking
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
            "call" -> showIncomingCallNotification(data)
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

    private fun showIncomingCallNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        Log.d("MyFirebaseService", "Incoming call from: $from")

        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("from", from)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contactName = runBlocking {
            ContactHelper.getContactName(applicationContext, from)
        }
        val notification = NotificationCompat.Builder(this, "incoming_call_channel")
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle("Incoming Call")
            .setContentText("From $contactName. Tap to open ZoiPer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun showMissedCallNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        addCallerToDb(from, "Missed", applicationContext)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "callHistory")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contactName = runBlocking {
            ContactHelper.getContactName(applicationContext, from)
        }
        val notification = NotificationCompat.Builder(this, "default_channel_id")
            .setSmallIcon(android.R.drawable.sym_call_missed)
            .setContentTitle("Missed call")
            .setContentText("From $contactName")
            .setStyle(NotificationCompat.BigTextStyle().bigText("From $contactName"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun showIncomingSmsNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        val body = data["body"] ?: ""
        val fromNormalized = ContactHelper.normalizeNumber(from)
        // addSmsToDb(from, body, applicationContext)
        // new sms system
        SmsRepository.insertFirebaseMessage(applicationContext, from, body, System.currentTimeMillis())

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // putExtra("destination", "inbox")
            putExtra("destination", "conversation/{$fromNormalized}")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contactName = runBlocking {
            ContactHelper.getContactName(applicationContext, from)
        }
        val notification = NotificationCompat.Builder(this, "sms_channel")
            .setSmallIcon(android.R.drawable.sym_action_email)
            .setContentTitle("SMS from $contactName")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Random.nextInt(), notification)
    }
}
