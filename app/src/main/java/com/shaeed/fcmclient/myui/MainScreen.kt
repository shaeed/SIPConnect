package com.shaeed.fcmclient.myui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.shaeed.fcmclient.myui.sms.ConversationScreen
import com.shaeed.fcmclient.myui.sms.InboxScreen
import com.shaeed.fcmclient.myui.sms.NewMessageScreen

@Composable
fun MainScreenOld(navController: NavController) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                "📱 SIP Client Config App",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ActionButton(
                    label = "Call History",
                    icon = Icons.Default.History
                ) { navController.navigate("callHistory") }
//                ActionButton(
//                    label = "SMS History",
//                    icon = Icons.Default.Email
//                ) { navController.navigate("smsHistory") }
                ActionButton(
                    label = "SMS Inbox",
                    icon = Icons.AutoMirrored.Filled.Message
                ) { navController.navigate("inbox") }
                ActionButton(
                    label = "Settings",
                    icon = Icons.Default.Settings
                ) { navController.navigate("settings") }
            }

            InfoCard()
        }
    }
}

@Composable
fun ActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Text(
            text = "Please install the ZoiPer app and configure it with your SIP account. This app registers your device with the SIP server to get call notifications and invoke ZoiPer automatically.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

data class NavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavHostController) {

    val navItems = listOf(
        NavItem("Call History", "callHistory", Icons.Default.History),
        NavItem("Inbox", "inbox", Icons.AutoMirrored.Filled.Message),
        NavItem("Settings", "settings", Icons.Default.Settings)
    )

    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry.value?.destination?.route ?: "inbox"
    val currentLabel = navItems.find { it.route == currentDestination }?.label ?: "Inbox"

    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text(text = currentLabel) }
//            )
//        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "inbox",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("callHistory") { CallHistoryScreen(navController) }
            composable("inbox") { InboxScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("inbox/newMessage") { NewMessageScreen(navController) }
            composable("conversation/{contact}/{contactNormalized}",
                arguments = listOf(
                    navArgument("contact") { type = NavType.StringType },
                    navArgument("contactNormalized") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val senderNormalized = backStackEntry.arguments?.getString("contactNormalized")!!
                val sender = backStackEntry.arguments?.getString("contact")!!
                ConversationScreen(navController, senderNormalized, sender)
            }
            composable("about") { AboutScreen(navController) }
        }
    }
}
