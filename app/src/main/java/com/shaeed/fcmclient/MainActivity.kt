package com.shaeed.fcmclient

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.shaeed.fcmclient.data.AppMode
import com.shaeed.fcmclient.myui.MainScreen
import com.shaeed.fcmclient.sms.DefaultSmsHelper
import com.shaeed.fcmclient.ui.theme.SIPConnectTheme
import com.shaeed.fcmclient.util.PermissionsHelper
import android.app.role.RoleManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannels(this)

        if (!DefaultSmsHelper.isDefaultSmsApp(this)) {
            DefaultSmsHelper.requestDefaultSmsApp(this)
        }

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
                MainScreen(navController)
            }
            PermissionsHelper.RequestAllPermissionsIfNeeded()
            ActivityCompat.requestPermissions(this, PermissionsHelper.REQUIRED_PERMISSIONS, 1)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && GlobalConfig.compileMode == AppMode.SMS_MANAGER) {
                val roleManager = getSystemService(RoleManager::class.java)
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

fun createNotificationChannels(context: Context) {
    val nm = context.getSystemService(NotificationManager::class.java)
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

object GlobalConfig {
    var compileMode: String = AppMode.SMS_MANAGER // AppMode.NORMAL // AppMode.SMS_MANAGER
    // var appMode: String = AppMode.NORMAL
}