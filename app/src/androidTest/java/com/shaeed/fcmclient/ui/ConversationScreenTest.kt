package com.shaeed.fcmclient.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.shaeed.fcmclient.data.FakeMessageRepository
import com.shaeed.fcmclient.data.MessageEntity
import com.shaeed.fcmclient.data.MessageType
import com.shaeed.fcmclient.myui.sms.ConversationScreen
import com.shaeed.fcmclient.viewmodel.ConversationViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationScreenTest {

    @get:Rule val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var fakeRepo: FakeMessageRepository
    private lateinit var viewModel: ConversationViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeMessageRepository()
        viewModel = ConversationViewModel(fakeRepo)
    }

    @Test
    fun screenRenders_withSenderInTopBar() {
        composeTestRule.setContent {
            ConversationScreen(
                navController = rememberNavController(),
                senderNormalized = "+1234567890",
                sender = "+1234567890",
                viewModel = viewModel
            )
        }
        composeTestRule.onNodeWithText("+1234567890").assertIsDisplayed()
    }

    @Test
    fun composeMessageBar_isDisplayed() {
        composeTestRule.setContent {
            ConversationScreen(
                navController = rememberNavController(),
                senderNormalized = "+1111",
                sender = "+1111",
                viewModel = viewModel
            )
        }
        composeTestRule.onNodeWithText("Type a message…").assertIsDisplayed()
    }

    @Test
    fun incomingMessages_areDisplayedAsBubbles() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+1234567890", senderNormalized = "+1234567890",
                body = "Hey there!", timestamp = 1_000_000L,
                type = MessageType.INCOMING_GSM, read = true
            ),
            MessageEntity(
                id = 2, threadId = 1,
                sender = "+1234567890", senderNormalized = "+1234567890",
                body = "How are you?", timestamp = 2_000_000L,
                type = MessageType.INCOMING_GSM, read = true
            )
        ))
        composeTestRule.setContent {
            ConversationScreen(
                navController = rememberNavController(),
                senderNormalized = "+1234567890",
                sender = "+1234567890",
                viewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                hasText("Hey there!")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Hey there!").assertIsDisplayed()
        composeTestRule.onNodeWithText("How are you?").assertIsDisplayed()
    }

    @Test
    fun outgoingMessage_isDisplayed() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+1234567890", senderNormalized = "+1234567890",
                body = "Sent by me", timestamp = 1_000_000L,
                type = MessageType.OUTGOING, read = true
            )
        ))
        composeTestRule.setContent {
            ConversationScreen(
                navController = rememberNavController(),
                senderNormalized = "+1234567890",
                sender = "+1234567890",
                viewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                hasText("Sent by me")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Sent by me").assertIsDisplayed()
    }

    @Test
    fun longPressMessage_showsOptionsDialog() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+1234567890", senderNormalized = "+1234567890",
                body = "Long press me", timestamp = 1_000_000L,
                type = MessageType.INCOMING_GSM, read = true
            )
        ))
        composeTestRule.setContent {
            ConversationScreen(
                navController = rememberNavController(),
                senderNormalized = "+1234567890",
                sender = "+1234567890",
                viewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                hasText("Long press me")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Long press me").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Message Options").assertIsDisplayed()
    }

    @Test
    fun messageOptionsDialog_hasCopyAndDeleteButtons() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+1234567890", senderNormalized = "+1234567890",
                body = "Options test", timestamp = 1_000_000L,
                type = MessageType.INCOMING_GSM, read = true
            )
        ))
        composeTestRule.setContent {
            ConversationScreen(
                navController = rememberNavController(),
                senderNormalized = "+1234567890",
                sender = "+1234567890",
                viewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(
                hasText("Options test")
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Options test").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Copy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
    }
}
