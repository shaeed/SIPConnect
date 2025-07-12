package com.shaeed.fcmclient

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
                val destination = intent?.getStringExtra("destination")

                // Do the navigation only once when the Composable is launched
                LaunchedEffect(destination) {
                    if (destination != null) {
                        navController.navigate(destination)
                    }
                }

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
            RequestNotificationPermissionIfNeeded()
            requestContactPermission(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
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

@Composable
fun RequestNotificationPermissionIfNeeded() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}