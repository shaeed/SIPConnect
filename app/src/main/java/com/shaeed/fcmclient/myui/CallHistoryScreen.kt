package com.shaeed.fcmclient.myui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shaeed.fcmclient.data.AppDatabase
import com.shaeed.fcmclient.getContactName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).callLogDao() }
    val callLogs by dao.getAll().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📞 Call History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(callLogs) { log ->
                Column(modifier = Modifier.padding(8.dp)) {
                    PhoneNumberItem(log.status, log.phoneNumber)
                    //Text("${SimpleDateFormat("EEE dd MMM HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))}")
                    //HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Text(
                        text = SimpleDateFormat("EEE dd MMM HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    HorizontalDivider(Modifier.padding(top = 8.dp), DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

@Composable
fun PhoneNumberItem(status: String, phoneNumber: String) {
    val context = LocalContext.current
    val contactName = getContactName(context, phoneNumber) ?: phoneNumber
    Text(
        text = "${status}: $contactName",
        modifier = Modifier.clickable {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Phone Number", phoneNumber)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Phone number copied", Toast.LENGTH_SHORT).show()
        }
    )
}
