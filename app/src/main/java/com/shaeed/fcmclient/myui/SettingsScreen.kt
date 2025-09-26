package com.shaeed.fcmclient.myui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import com.shaeed.fcmclient.network.RetrofitClient
import com.shaeed.fcmclient.data.PrefKeys
import com.shaeed.fcmclient.data.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val token = getFCMToken()
    var serverResponse by remember { mutableStateOf("") }
    val context = LocalContext.current
    val rememberCoroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){ checkAndReregister(context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("about") }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("FCM Token", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = token,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        rememberCoroutineScope.launch {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Token", token)
                            clipboard.setPrimaryClip(clip)
                        }

                    }) {
                        Text("Copy Token")
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    var ipAddress by remember {
                        mutableStateOf(SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS))
                    }
                    var username by remember {
                        mutableStateOf(SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER))
                    }
                    var userPass by remember {
                        mutableStateOf(SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_PASS))
                    }

                    Text("Server Configuration", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("Server Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("SIP username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = userPass,
                        onValueChange = { userPass = it },
                        label = { Text("SIP Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = {
                        SharedPreferences.saveKeyValue(context, PrefKeys.IP_ADDRESS, ipAddress)
                        SharedPreferences.saveKeyValue(context, PrefKeys.SIP_SERVER_USER, username)
                        SharedPreferences.saveKeyValue(context, PrefKeys.SIP_SERVER_PASS, userPass)
                        CoroutineScope(Dispatchers.IO).launch {
                            serverResponse = RetrofitClient.uploadFCMToServer(context, token)
                        }
                    }) {
                        Text("Save & Connect")
                    }
                }
            }

            if (serverResponse.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = serverResponse,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Restart SIPConnect Server",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Warning: This will restart the SIPConnect server (asterisk). Please" +
                                " restart only if its not responding or there is errors." +
                                " Restarting it will end any active call connected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                serverResponse = RetrofitClient.restartSip(context)
                                Log.d("SettingsScreen", serverResponse)
                            }
                        },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Danger",
                            tint = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add some spacing
                        Text("Restart", color = MaterialTheme.colorScheme.onError)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun getFCMToken(): String {
    var token by remember { mutableStateOf("Fetching token...") }
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            token = it
        }
    }
    return token
}

suspend fun checkAndReregister(context: Context) {
    if (SharedPreferences.getKeyValue(context, PrefKeys.IP_ADDRESS) == ""
        || SharedPreferences.getKeyValue(context, PrefKeys.SIP_SERVER_USER) == "") {
        return
    }

    val token = FirebaseMessaging.getInstance().token.await()
    val serverToken = RetrofitClient.getTokenFromServer(context)
    if (serverToken == null || serverToken != token) {
        RetrofitClient.uploadFCMToServer(context, token)
    }
}
