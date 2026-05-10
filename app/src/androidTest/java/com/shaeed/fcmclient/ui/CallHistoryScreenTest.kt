package com.shaeed.fcmclient.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.shaeed.fcmclient.data.CallLog
import com.shaeed.fcmclient.data.FakeCallLogRepository
import com.shaeed.fcmclient.myui.CallHistoryScreen
import com.shaeed.fcmclient.viewmodel.CallViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CallHistoryScreenTest {

    @get:Rule val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var fakeRepo: FakeCallLogRepository
    private lateinit var viewModel: CallViewModel

    @Before
    fun setUp() {
        fakeRepo = FakeCallLogRepository()
        viewModel = CallViewModel(fakeRepo)
    }

    @Test
    fun emptyList_screenRendersWithTopBar() {
        composeTestRule.setContent {
            CallHistoryScreen(
                navController = rememberNavController(),
                callViewModel = viewModel
            )
        }
        composeTestRule.onNodeWithText("Call History", substring = true).assertIsDisplayed()
    }

    @Test
    fun incomingCall_phoneNumberAndStatusDisplayed() {
        fakeRepo.emit(listOf(
            CallLog(
                phoneNumber = "+1234567890",
                normalizedNumber = "+1234567890",
                timestamp = 1_000_000L,
                status = "Incoming"
            )
        ))
        composeTestRule.setContent {
            CallHistoryScreen(
                navController = rememberNavController(),
                callViewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("+1234567890")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("+1234567890").assertIsDisplayed()
        composeTestRule.onNodeWithText("Incoming").assertIsDisplayed()
    }

    @Test
    fun outgoingCall_statusDisplayed() {
        fakeRepo.emit(listOf(
            CallLog(
                phoneNumber = "555-1234",
                normalizedNumber = "555-1234",
                timestamp = 1_000_000L,
                status = "Outgoing"
            )
        ))
        composeTestRule.setContent {
            CallHistoryScreen(
                navController = rememberNavController(),
                callViewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("Outgoing")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Outgoing").assertIsDisplayed()
    }

    @Test
    fun missedCall_statusDisplayed() {
        fakeRepo.emit(listOf(
            CallLog(
                phoneNumber = "555-9999",
                normalizedNumber = "555-9999",
                timestamp = 1_000_000L,
                status = "Missed"
            )
        ))
        composeTestRule.setContent {
            CallHistoryScreen(
                navController = rememberNavController(),
                callViewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("Missed")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Missed").assertIsDisplayed()
    }

    @Test
    fun rejectedCall_statusDisplayed() {
        fakeRepo.emit(listOf(
            CallLog(
                phoneNumber = "555-0000",
                normalizedNumber = "555-0000",
                timestamp = 1_000_000L,
                status = "Rejected"
            )
        ))
        composeTestRule.setContent {
            CallHistoryScreen(
                navController = rememberNavController(),
                callViewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("Rejected")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Rejected").assertIsDisplayed()
    }

    @Test
    fun multipleCallLogs_allPhoneNumbersDisplayed() {
        fakeRepo.emit(listOf(
            CallLog(id = 1, phoneNumber = "+1111", normalizedNumber = "+1111", timestamp = 2_000_000L, status = "Incoming"),
            CallLog(id = 2, phoneNumber = "+2222", normalizedNumber = "+2222", timestamp = 1_000_000L, status = "Outgoing")
        ))
        composeTestRule.setContent {
            CallHistoryScreen(
                navController = rememberNavController(),
                callViewModel = viewModel
            )
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("+1111")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("+1111").assertIsDisplayed()
        composeTestRule.onNodeWithText("+2222").assertIsDisplayed()
    }
}
