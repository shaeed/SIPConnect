package com.shaeed.fcmclient.myui.sms

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shaeed.fcmclient.data.DeliveryStatus
import com.shaeed.fcmclient.data.MessageEntity
import com.shaeed.fcmclient.data.MessageType
import com.shaeed.fcmclient.util.UtilFunctions.formatTimestamp
import com.shaeed.fcmclient.viewmodel.ContactViewModel
import com.shaeed.fcmclient.viewmodel.ContactViewModelFactory
import com.shaeed.fcmclient.viewmodel.ConversationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    navController: NavController,
    senderNormalized: String,
    viewModel: ConversationViewModel = viewModel()
) {
    val messages by viewModel.getMessages(senderNormalized).collectAsState(emptyList())
    val listState = rememberLazyListState()
    val contactViewModel: ContactViewModel = viewModel(factory = ContactViewModelFactory(LocalContext.current))
    val phonebook by contactViewModel.phonebook.collectAsState()
    val contactName = phonebook[senderNormalized] ?: senderNormalized

    val didInitialScroll = remember { mutableStateOf(true) }
//    LaunchedEffect(messages) {
//        didInitialScroll.value = true
//        if (!didInitialScroll.value && messages.isNotEmpty()) {
//            listState.scrollToItem(messages.lastIndex)
//            Log.d("css", "Scrolled")
//        }
//    }

    LaunchedEffect(senderNormalized) { viewModel.markAsRead(senderNormalized) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            var showDeleteDialog by remember { mutableStateOf<Pair<Boolean, Long?>>(false to null) }
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        message = msg,
                        onLongPress = { showDeleteDialog = true to msg.id }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            if (showDeleteDialog.first) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false to null },
                    title = { Text("Delete Message") },
                    text = { Text("Are you sure you want to delete this message?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog.second?.let { id ->
                                viewModel.deleteMessage(id)
                            }
                            showDeleteDialog = false to null
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false to null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            ComposeMessageBar(
                onSend = { text ->
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.sendMessage(messages[0].sender, text)
                    }
                    didInitialScroll.value = false
                }
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onLongPress: (MessageEntity) -> Unit
) {
    val isOutgoing = message.type == MessageType.OUTGOING
    val bubbleColor = if (isOutgoing)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant
    val alignment = if (isOutgoing) Alignment.End else Alignment.Start

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        coroutineScope.launch {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SMS Message", message.body)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onLongClick = { onLongPress(message) }
                )
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(max = 300.dp)
        ) {
            Text(
                text = message.body,
                color = if (isOutgoing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (!message.read) FontWeight.Bold else FontWeight.Normal
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))
            val simName = when (message.simId) {
                0 -> "SIM 1"
                1 -> "SIM 2"
                10 -> "Firebase"
                else -> "SIM ${message.simId}"
            }
            Text(
                text = simName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))
            // Delivery Status (only for outgoing messages)
            /*if (isOutgoing) {
                val statusText = when (message.deliveryStatus) {
                    DeliveryStatus.PENDING -> "Pending"
                    DeliveryStatus.SENT -> "Sent"
                    DeliveryStatus.DELIVERED -> "Delivered"
                    DeliveryStatus.FAILED -> "Failed"
                }
                val statusColor = when (message.deliveryStatus) {
                    DeliveryStatus.PENDING -> Color.Gray
                    DeliveryStatus.SENT -> Color.Blue
                    DeliveryStatus.DELIVERED -> Color.Green
                    DeliveryStatus.FAILED -> Color.Red
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }*/

            // Status icon (only for outgoing messages)
            if (isOutgoing) {
                Spacer(modifier = Modifier.width(8.dp))
                val icon = when (message.deliveryStatus) {
                    DeliveryStatus.PENDING -> Icons.Default.Check
                    DeliveryStatus.SENT -> Icons.Default.DoneAll
                    DeliveryStatus.DELIVERED -> Icons.Default.DoneAll
                    DeliveryStatus.FAILED -> Icons.Default.Check
                }
                val iconColor = when (message.deliveryStatus) {
                    DeliveryStatus.PENDING -> Color.Gray
                    DeliveryStatus.SENT -> Color.Gray
                    DeliveryStatus.DELIVERED -> Color(0xFF1E88E5) // blue double tick
                    DeliveryStatus.FAILED -> Color.Red
                }
                Icon(
                    imageVector = icon,
                    contentDescription = message.deliveryStatus.name,
                    modifier = Modifier.size(16.dp),
                    tint = iconColor
                )
            }
        }
    }
}
