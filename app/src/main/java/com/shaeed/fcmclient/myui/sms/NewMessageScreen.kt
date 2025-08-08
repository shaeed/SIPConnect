package com.shaeed.fcmclient.myui.sms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shaeed.fcmclient.sms.SmsSender
import com.shaeed.fcmclient.viewmodel.ContactViewModel
import com.shaeed.fcmclient.viewmodel.ContactViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(
    navController: NavController,
    contactViewModel: ContactViewModel = viewModel(factory = ContactViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val phonebook by contactViewModel.phonebook.collectAsState()
    var recipient by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val matchingContacts = phonebook.filter { (number, name) ->
        number.contains(recipient, ignoreCase = true) || name.contains(recipient, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Message") },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("To:", style = MaterialTheme.typography.labelMedium)
                TextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    placeholder = { Text("Enter number or name") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (recipient.isNotBlank() && matchingContacts.isNotEmpty()) {
                    Column {
                        matchingContacts.forEach { (number, name) ->
                            TextButton(
                                onClick = {
                                    recipient = number
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("$name ($number)")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("Message:", style = MaterialTheme.typography.labelMedium)
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Type your message…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Button(
                onClick = {
                    if (recipient.isNotBlank() && message.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            SmsSender.send(context, recipient, message)
                        }
                        navController.popBackStack()
                        //navController.navigate("conversation/${recipient}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = recipient.isNotBlank() && message.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}