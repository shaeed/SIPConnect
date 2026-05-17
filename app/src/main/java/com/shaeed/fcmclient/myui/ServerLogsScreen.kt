package com.shaeed.fcmclient.myui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shaeed.fcmclient.network.RetrofitClient
import com.shaeed.fcmclient.network.ServerCallLog
import com.shaeed.fcmclient.network.ServerSmsLog
import com.shaeed.fcmclient.util.ContactHelper
import com.shaeed.fcmclient.viewmodel.ContactViewModel
import com.shaeed.fcmclient.viewmodel.ContactViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Server sends timestamps in UTC without timezone marker
private fun formatTimestamp(raw: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(raw) ?: return raw
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date)
    } catch (e: Exception) {
        raw
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
}

private data class ServerCallDetail(
    val normalizedNumber: String,
    val displayNumber: String,
    val calls: List<ServerCallLog>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerLogsScreen(navController: NavController) {
    val context = LocalContext.current
    val contactViewModel: ContactViewModel = viewModel(factory = ContactViewModelFactory(context))
    var selectedTab by remember { mutableIntStateOf(0) }
    var smsLogs by remember { mutableStateOf<List<ServerSmsLog>>(emptyList()) }
    var callLogs by remember { mutableStateOf<List<ServerCallLog>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            withContext(Dispatchers.IO) {
                smsLogs = RetrofitClient.getSmsLogs(context)
                callLogs = RetrofitClient.getCallLogs(context)
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Server Logs") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("SMS") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Calls") },
                    icon = { Icon(Icons.Default.Call, contentDescription = null) }
                )
            }

            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = MaterialTheme.colorScheme.error)
                }
                selectedTab == 0 -> SmsLogsList(smsLogs, contactViewModel)
                else -> CallLogsList(callLogs, contactViewModel)
            }
        }
    }
}

@Composable
private fun SmsLogsList(logs: List<ServerSmsLog>, contactViewModel: ContactViewModel) {
    if (logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No SMS logs", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        items(logs) { log -> SmsLogItem(log, contactViewModel) }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun SmsLogItem(log: ServerSmsLog, contactViewModel: ContactViewModel) {
    val context = LocalContext.current
    val phonebook by contactViewModel.phonebook.collectAsState()
    val contactName = phonebook[ContactHelper.normalizeNumber(log.number)]

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    if (contactName != null) {
                        Text(contactName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(log.number, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(log.number, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(log.sms_type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    log.message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { copyToClipboard(context, "SMS", log.message) }) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy message",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                Text(
                    "User: ${log.user}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallLogsList(logs: List<ServerCallLog>, contactViewModel: ContactViewModel) {
    val context = LocalContext.current
    var detailState by remember { mutableStateOf<ServerCallDetail?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Precompute per-number groups so each item can show count and all calls on tap
    val callsByNumber = remember(logs) {
        logs.groupBy { ContactHelper.normalizeNumber(it.number) }
    }

    if (logs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No call logs", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        items(logs) { log ->
            val normalizedNumber = ContactHelper.normalizeNumber(log.number)
            val allCalls = callsByNumber[normalizedNumber] ?: listOf(log)
            ServerCallLogItem(
                log = log,
                count = allCalls.size,
                contactViewModel = contactViewModel,
                onTap = {
                    detailState = ServerCallDetail(
                        normalizedNumber = normalizedNumber,
                        displayNumber = log.number,
                        calls = allCalls.sortedByDescending { it.timestamp }
                    )
                }
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
    }

    detailState?.let { detail ->
        val phonebook by contactViewModel.phonebook.collectAsState()
        val contactName = phonebook[detail.normalizedNumber]

        ModalBottomSheet(
            onDismissRequest = { detailState = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        if (contactName != null) {
                            Text(contactName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Text(detail.displayNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(detail.displayNumber, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                    IconButton(onClick = { copyToClipboard(context, "Number", detail.displayNumber) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy number", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = "${detail.calls.size} call${if (detail.calls.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                detail.calls.forEach { call ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                formatTimestamp(call.timestamp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "User: ${call.user}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerCallLogItem(
    log: ServerCallLog,
    count: Int,
    contactViewModel: ContactViewModel,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    val phonebook by contactViewModel.phonebook.collectAsState()
    val contactName = phonebook[ContactHelper.normalizeNumber(log.number)]

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (count > 1) {
                        Badge { Text(count.toString()) }
                    }
                }
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                if (contactName != null) {
                    Text(contactName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(log.number, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(log.number, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "User: ${log.user}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { copyToClipboard(context, "Number", log.number) }) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy number",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
