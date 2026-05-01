package com.shaeed.fcmclient

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shaeed.fcmclient.data.AppMode
import com.shaeed.fcmclient.data.PrefKeys
import com.shaeed.fcmclient.data.SharedPreferences
import com.shaeed.fcmclient.data.SmsRepository
import com.shaeed.fcmclient.data.addCallerToDb
import com.shaeed.fcmclient.myui.IncomingCallActivity
import com.shaeed.fcmclient.sms.SmsSender
import com.shaeed.fcmclient.util.ContactHelper
import com.shaeed.fcmclient.util.UtilFunctions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MyFirebaseService : FirebaseMessagingService() {
    val INCOMING_CALL_NOTIFICATION_ID = 1001

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
            putExtra("timestamp", data["timestamp"])
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
        notificationManager.notify(INCOMING_CALL_NOTIFICATION_ID, notification)
    }

    private fun showMissedCallNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        val timestamp = UtilFunctions.isoToMillis(data["timestamp"] ?: "unknown")
        addCallerToDb(from, "Missed", timestamp, applicationContext)

        // Cancel the ongoing incoming call notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID)

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

        // Missed call still use random ID to show multiple missed call notifications
        notificationManager.notify(Random.nextInt(), notification)
    }

    private fun showIncomingSmsNotification(data: Map<String, String>) {
        val from = data["phone_number"] ?: "Unknown"
        val body = data["body"] ?: ""
        val isOutgoingGsm = data["forward_to_gsm"]?.toBoolean() ?: false
        val timestamp = UtilFunctions.isoToMillis(data["timestamp"] ?: "unknown")
        val fromNormalized = ContactHelper.normalizeNumber(from)
        val notificationId = fromNormalized.hashCode()

        if(isOutgoingGsm) {
            if (SharedPreferences.getKeyValue(applicationContext, PrefKeys.APP_MODE) == AppMode.SERVER){
                CoroutineScope(Dispatchers.IO).launch {
                    SmsSender.send(applicationContext, from, body)
                }
            } else {
                // outgoing sms via firebase coming to app and app is not in server mode
                // don't show this notification to user or register in the app
                Log.d("FCM", "GSM targeted message received on client.")
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val messageId = SmsRepository.insertFirebaseMessage(applicationContext, from, body, timestamp)
            val contactName = ContactHelper.getContactName(applicationContext, from)
            val otp = UtilFunctions.extractOtp(body)
            val collapsedText = if (otp != null) "OTP: ${UtilFunctions.formatOtp(otp)}" else body

            val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("destination", "conversation/$from/$fromNormalized")
            }
            val openPendingIntent = PendingIntent.getActivity(
                applicationContext, notificationId, openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val deleteIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_DELETE
                putExtra(NotificationActionReceiver.EXTRA_MESSAGE_ID, messageId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val deletePendingIntent = PendingIntent.getBroadcast(
                applicationContext, notificationId + 1, deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val markReadIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_READ
                putExtra(NotificationActionReceiver.EXTRA_SENDER_NORMALIZED, fromNormalized)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val markReadPendingIntent = PendingIntent.getBroadcast(
                applicationContext, notificationId + 2, markReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(applicationContext, "sms_channel")
                .setSmallIcon(android.R.drawable.sym_action_email)
                .setContentTitle("SMS from $contactName")
                .setContentText(collapsedText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(openPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_delete, "Delete", deletePendingIntent)
                .addAction(android.R.drawable.ic_menu_view, "Mark as Read", markReadPendingIntent)
                .build()

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        }
    }
}
