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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shaeed.fcmclient.ContactHelper.normalizeNumber
import com.shaeed.fcmclient.ContactViewModel
import com.shaeed.fcmclient.ContactViewModelFactory
import com.shaeed.fcmclient.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).smsLogDao() }
    val smsLogs by dao.getAll().collectAsState(initial = emptyList())
    val viewModel: ContactViewModel = viewModel(factory = ContactViewModelFactory(context))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 SMS Messages") },
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
            items(smsLogs) { sms ->
                Column(modifier = Modifier.padding(8.dp)) {
                    val phonebook by viewModel.phonebook.collectAsState()
                    val normalized = normalizeNumber(sms.from)
                    val contactName = phonebook[normalized] ?: sms.from
                    Text(
                        text = "From: $contactName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    SmsBody(sms.body)
                    Text(
                        text = SimpleDateFormat("EEE dd MMM HH:mm:ss", Locale.getDefault()).format(Date(sms.timestamp)),
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
fun SmsBody(body: String) {
    val context = LocalContext.current
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clickable {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SMS Message", body)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
        }.padding(vertical = 4.dp)
    )
}
