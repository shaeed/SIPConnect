package com.shaeed.fcmclient.myui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shaeed.fcmclient.data.GroupedCallLog
import com.shaeed.fcmclient.util.UtilFunctions.formatTimestamp
import com.shaeed.fcmclient.viewmodel.CallViewModel
import com.shaeed.fcmclient.viewmodel.CallViewModelFactory
import com.shaeed.fcmclient.viewmodel.ContactViewModel
import com.shaeed.fcmclient.viewmodel.ContactViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    navController: NavController,
    callViewModel: CallViewModel = viewModel(factory = CallViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val groupedCallLogs by callViewModel.groupedCallLogs.collectAsState()
    val contactViewModel: ContactViewModel = viewModel(factory = ContactViewModelFactory(context))
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedGroup by remember { mutableStateOf<GroupedCallLog?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        callViewModel.deleteOldCallLogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📞 Call History") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(groupedCallLogs, key = { it.normalizedNumber }) { group ->
                GroupedCallLogItem(
                    group = group,
                    viewModel = contactViewModel,
                    onTap = { selectedGroup = group }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )
            }
        }
    }

    selectedGroup?.let { group ->
        val phonebook by contactViewModel.phonebook.collectAsState()
        val contactName = phonebook[group.normalizedNumber] ?: group.phoneNumber

        ModalBottomSheet(
            onDismissRequest = { selectedGroup = null },
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
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", group.phoneNumber))
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar("Phone number copied")
                        }
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy number")
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, "tel:${group.phoneNumber}".toUri()).apply {
                            setPackage("com.zoiper.android.app")
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "ZoiPer not installed.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${group.count} call${if (group.count > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                group.calls.forEach { call ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (call.status) {
                                "Incoming" -> Icons.AutoMirrored.Filled.CallReceived
                                "Outgoing" -> Icons.AutoMirrored.Filled.CallMade
                                "Missed", "Rejected" -> Icons.AutoMirrored.Filled.CallMissed
                                else -> Icons.Filled.Phone
                            },
                            contentDescription = call.status,
                            tint = when (call.status) {
                                "Incoming" -> MaterialTheme.colorScheme.primary
                                "Outgoing" -> MaterialTheme.colorScheme.secondary
                                "Missed", "Rejected" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = call.status,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = formatTimestamp(call.timestamp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupedCallLogItem(
    group: GroupedCallLog,
    viewModel: ContactViewModel,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    val phonebook by viewModel.phonebook.collectAsState()
    val contactName = phonebook[group.normalizedNumber] ?: group.phoneNumber

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgedBox(
            badge = {
                if (group.count > 1) {
                    Badge { Text(group.count.toString()) }
                }
            }
        ) {
            Icon(
                imageVector = when (group.latestStatus) {
                    "Incoming" -> Icons.AutoMirrored.Filled.CallReceived
                    "Outgoing" -> Icons.AutoMirrored.Filled.CallMade
                    "Missed", "Rejected" -> Icons.AutoMirrored.Filled.CallMissed
                    else -> Icons.Filled.Phone
                },
                contentDescription = group.latestStatus,
                tint = when (group.latestStatus) {
                    "Incoming" -> MaterialTheme.colorScheme.primary
                    "Outgoing" -> MaterialTheme.colorScheme.secondary
                    "Missed", "Rejected" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contactName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Text(
                text = formatTimestamp(group.latestTimestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = group.latestStatus,
            style = MaterialTheme.typography.bodyMedium,
            color = when (group.latestStatus) {
                "Incoming" -> MaterialTheme.colorScheme.primary
                "Outgoing" -> MaterialTheme.colorScheme.secondary
                "Missed", "Rejected" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        IconButton(onClick = {
            val intent = Intent(Intent.ACTION_DIAL, "tel:${group.phoneNumber}".toUri()).apply {
                setPackage("com.zoiper.android.app")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "ZoiPer not installed.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "Call",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
