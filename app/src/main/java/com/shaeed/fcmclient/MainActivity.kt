package com.shaeed.fcmclient

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shaeed.fcmclient.myui.CallHistoryScreen
import com.shaeed.fcmclient.myui.FcmTokenScreen
import com.shaeed.fcmclient.myui.MainScreen
import com.shaeed.fcmclient.myui.SmsHistoryScreen
import com.shaeed.fcmclient.ui.theme.SIPConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SIPConnectTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(navController)
                    }
                    composable("callHistory") {
                        CallHistoryScreen(navController)
                    }
                    composable("smsHistory") {
                        SmsHistoryScreen(navController)
                    }
                    composable("fcmToken") {
                        FcmTokenScreen(navController)
                    }
                }
            }
            CreateCallNotificationChannel()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SIPConnectTheme {
        Greeting("Android")
    }
}

@Composable
fun CreateCallNotificationChannel() {
    // Create notification channel for Android 8.0+
    val nm = LocalContext.current.getSystemService(NotificationManager::class.java)
    val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    val channel = NotificationChannel(
        "incoming_call_channel",
        "Incoming Calls",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Channel for incoming VoIP calls"
        enableLights(true)
        enableVibration(true)
        setSound(ringtoneUri,
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }
    nm.createNotificationChannel(channel)

    // SMS
    val smsChannel = NotificationChannel(
        "sms_channel",
        "SMS Notifications",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Notifications for received SIP SMS"
        enableLights(true)
        enableVibration(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }
    nm.createNotificationChannel(smsChannel)

    // Default
    val defaultChannel = NotificationChannel(
        "default_channel_id",
        "Default Channel",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Default Notification channel"
        enableLights(true)
        enableVibration(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }
    nm.createNotificationChannel(defaultChannel)
}
