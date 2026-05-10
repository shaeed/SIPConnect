package com.shaeed.fcmclient.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.shaeed.fcmclient.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    @Test
    fun bottomNav_hasThreeTabs() {
        composeTestRule.onNodeWithText("Call History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Inbox").assertIsDisplayed()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun app_startsOnInboxScreen() {
        // NavHost startDestination is "inbox" — FAB is the inbox-specific element
        composeTestRule.onNodeWithContentDescription("New Message").assertIsDisplayed()
    }

    @Test
    fun callHistoryTab_navigatesToCallHistoryScreen() {
        composeTestRule.onNodeWithText("Call History").performClick()
        // CallHistoryScreen top bar contains "Call History"
        composeTestRule.onNodeWithText("Call History", substring = true).assertIsDisplayed()
    }

    @Test
    fun settingsTab_navigatesToSettingsScreen() {
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("FCM Token").assertIsDisplayed()
    }

    @Test
    fun inboxTab_navigatesBackToInboxScreen() {
        // Navigate away to Settings, then return to Inbox
        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Inbox").performClick()
        composeTestRule.onNodeWithContentDescription("New Message").assertIsDisplayed()
    }
}
