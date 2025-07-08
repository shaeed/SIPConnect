package com.shaeed.fcmclient.myui

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.messaging.FirebaseMessaging
import com.shaeed.fcmclient.RetrofitClient
import com.shaeed.fcmclient.data.PostRequest
import com.shaeed.fcmclient.data.PostResponse
import com.shaeed.fcmclient.data.PrefKeys
import com.shaeed.fcmclient.data.SharedPreferences
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FcmTokenScreen(navController: NavController) {
    val token = getFCMToken()
    var serverResponse by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔐 FCM Token") },
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
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("FCM Token:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = token,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .background(Color.LightGray)
                    .padding(8.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            Button(onClick = {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(token))
                Toast.makeText(context, "FCM token copied to clipboard", Toast.LENGTH_SHORT).show()
            }) {
                Text("Copy")
            }

            Spacer(modifier = Modifier.height(8.dp))
            var ipAddress by remember { mutableStateOf(SharedPreferences().getKeyValue(context, PrefKeys.IP_ADDRESS)) }
            TextField(
                value = ipAddress,
                onValueChange = { it -> ipAddress = it },
                label = { Text("Server Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            var userName by remember { mutableStateOf(SharedPreferences().getKeyValue(context, PrefKeys.SIP_SERVER_USER)) }
            TextField(
                value = userName,
                onValueChange = { it -> userName = it },
                label = { Text("User Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            var userPass by remember { mutableStateOf(SharedPreferences().getKeyValue(context, PrefKeys.SIP_SERVER_PASS)) }
            TextField(
                value = userPass,
                onValueChange = { it -> userPass = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                SharedPreferences().saveKeyValue(context, PrefKeys.IP_ADDRESS, ipAddress)
                SharedPreferences().saveKeyValue(context, PrefKeys.SIP_SERVER_USER, userName)
                SharedPreferences().saveKeyValue(context, PrefKeys.SIP_SERVER_PASS, userPass)
                uploadFCMToServer(ipAddress, userName, userPass, token) { result ->
                    Log.d("Result", result)
                    serverResponse = result
                }
            }) {
                Text("Connect")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = serverResponse,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun getFCMToken(): String {
    var token by remember { mutableStateOf("Fetching token...") }
    // Fetch FCM token
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            token = it
        }
    }

    Log.d(TAG, "FCM Token: $token")
    return token
}

fun uploadFCMToServer(server: String, userName: String, userPass: String, fcmToken: String, onResult: (String) -> Unit) {
    val deviceId = "${Build.MANUFACTURER} ${Build.MODEL}"
    val request = PostRequest(deviceId, userName, userPass, fcmToken)
    val url = "http://$server/sip/client/register"
    Log.d("FcmTokenScreen", "URL to upload FCM: $url")

    RetrofitClient.apiService.createPost(url, request)
        .enqueue(object : Callback<PostResponse> {
            override fun onResponse(call: Call<PostResponse>, response: Response<PostResponse>) {
                val result = if (response.isSuccessful) {
                    "Success! Device registered on server."
                } else {
                    "Error: ${response.code()}"
                }
                onResult(result)
            }

            override fun onFailure(call: Call<PostResponse>, t: Throwable) {
                val result = "Failure: ${t.message}"
                Log.e("TokenSave", result)
                onResult(result)
            }
        })
}

