package com.shaeed.fcmclient.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.shaeed.fcmclient.data.FakeMessageRepository
import com.shaeed.fcmclient.data.MessageEntity
import com.shaeed.fcmclient.data.MessageType
import com.shaeed.fcmclient.myui.sms.InboxScreen
import com.shaeed.fcmclient.viewmodel.InboxViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InboxScreenTest {

    @get:Rule val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var fakeRepo: FakeMessageRepository
    private lateinit var viewModel: InboxViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeMessageRepository()
        viewModel = InboxViewModel(fakeRepo)
    }

    @Test
    fun emptyInbox_screenRendersWithTopBar() {
        composeTestRule.setContent {
            InboxScreen(navController = rememberNavController(), viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
    }

    @Test
    fun fab_isDisplayed() {
        composeTestRule.setContent {
            InboxScreen(navController = rememberNavController(), viewModel = viewModel)
        }
        composeTestRule.onNodeWithContentDescription("New Message").assertIsDisplayed()
    }

    @Test
    fun conversation_isDisplayedWithSenderAndPreview() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+1234567890", senderNormalized = "+1234567890",
                body = "Hello, test message",
                timestamp = System.currentTimeMillis(),
                type = MessageType.INCOMING_GSM,
                read = false
            )
        ))
        composeTestRule.setContent {
            InboxScreen(navController = rememberNavController(), viewModel = viewModel)
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("+1234567890")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("+1234567890").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hello, test message", substring = true).assertIsDisplayed()
    }

    @Test
    fun multipleConversations_allSendersDisplayed() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+1111", senderNormalized = "+1111",
                body = "First message",
                timestamp = 2_000_000L,
                type = MessageType.INCOMING_GSM, read = true
            ),
            MessageEntity(
                id = 2, threadId = 2,
                sender = "+2222", senderNormalized = "+2222",
                body = "Second message",
                timestamp = 1_000_000L,
                type = MessageType.INCOMING_GSM, read = true
            )
        ))
        composeTestRule.setContent {
            InboxScreen(navController = rememberNavController(), viewModel = viewModel)
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("+1111")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("+1111").assertIsDisplayed()
        composeTestRule.onNodeWithText("+2222").assertIsDisplayed()
    }

    @Test
    fun longPressConversation_showsDeleteDialog() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+5551234", senderNormalized = "+5551234",
                body = "Delete test",
                timestamp = System.currentTimeMillis(),
                type = MessageType.INCOMING_GSM, read = false
            )
        ))
        composeTestRule.setContent {
            InboxScreen(navController = rememberNavController(), viewModel = viewModel)
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("+5551234")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("+5551234").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Delete Conversation").assertIsDisplayed()
    }

    @Test
    fun deleteDialog_cancelButtonDismissesDialog() {
        fakeRepo.emit(listOf(
            MessageEntity(
                id = 1, threadId = 1,
                sender = "+9999", senderNormalized = "+9999",
                body = "Cancel test",
                timestamp = System.currentTimeMillis(),
                type = MessageType.INCOMING_FIREBASE, read = true
            )
        ))
        composeTestRule.setContent {
            InboxScreen(navController = rememberNavController(), viewModel = viewModel)
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("+9999")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("+9999").performTouchInput { longClick() }
        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.onNodeWithText("Delete Conversation").assertDoesNotExist()
    }
}
