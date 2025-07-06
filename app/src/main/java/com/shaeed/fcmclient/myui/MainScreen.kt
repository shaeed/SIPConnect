package com.shaeed.fcmclient.myui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MainScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📱 SIP Client Config App", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("callHistory") }) {
            Text("Call History")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("smsHistory") }) {
            Text("SMS")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("fcmToken") }) {
            Text("FCM Token")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Please install ZoiPer app and configure it with SIP account. This App will " +
                    "be used to register this device with SIP server to get the " +
                    "notification of call, " +
                    "and invoke ZoiPer app.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .background(Color.LightGray)
                .padding(8.dp)
                .fillMaxWidth()
        )
    }
}
