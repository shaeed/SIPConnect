package com.shaeed.fcmclient.util

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionsHelper {
    val REQUIRED_PERMISSIONS: Array<String> get() {
        val base = mutableListOf(Manifest.permission.READ_CONTACTS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            base += Manifest.permission.POST_NOTIFICATIONS
        }
        return base.toTypedArray()
    }

    val SMS_PERMISSIONS = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE,
    )

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        } else {
            true
        }
    }

    fun allGranted(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @Composable
    fun RequestAllPermissionsIfNeeded(includeSms: Boolean = false) {
        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val denied = permissions.filterValues { !it }
            if (denied.isNotEmpty()) {
                Toast.makeText(context, "Some permissions denied: ${denied.keys}", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            val toRequest = buildList {
                addAll(REQUIRED_PERMISSIONS)
                if (includeSms) addAll(SMS_PERMISSIONS)
            }.filter {
                ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
            }
            if (toRequest.isNotEmpty()) {
                launcher.launch(toRequest.toTypedArray())
            }
        }
    }
}
