package com.shaeed.fcmclient.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shaeed.fcmclient.myui.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun topBar_displaysSettingsTitle() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Settings", substring = true).assertIsDisplayed()
    }

    @Test
    fun fcmTokenSection_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("FCM Token").assertIsDisplayed()
    }

    @Test
    fun copyTokenButton_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Copy Token").assertIsDisplayed()
    }

    @Test
    fun serverConfigSection_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Server Configuration").assertIsDisplayed()
    }

    @Test
    fun serverAddressField_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Server Address", substring = true).assertIsDisplayed()
    }

    @Test
    fun sipUsernameField_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("SIP username", substring = true).assertIsDisplayed()
    }

    @Test
    fun saveAndConnectButton_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Save & Connect").assertIsDisplayed()
    }

    @Test
    fun restartSipSection_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Restart SIPConnect Server").assertIsDisplayed()
    }

    @Test
    fun restartButton_isDisplayed() {
        composeTestRule.setContent {
            SettingsScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Restart").assertIsDisplayed()
    }
}
