package com.shaeed.fcmclient.ui

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.shaeed.fcmclient.data.FakeContactRepository
import com.shaeed.fcmclient.myui.sms.NewMessageScreen
import com.shaeed.fcmclient.viewmodel.ContactViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewMessageScreenTest {

    @get:Rule val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_CONTACTS)

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var viewModel: ContactViewModel

    @Before
    fun setUp() {
        viewModel = ContactViewModel(FakeContactRepository())
    }

    @Test
    fun screenRenders_withTopBarTitle() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("New Message").assertIsDisplayed()
    }

    @Test
    fun toLabel_isDisplayed() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("To:").assertIsDisplayed()
    }

    @Test
    fun messageLabel_isDisplayed() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Message:").assertIsDisplayed()
    }

    @Test
    fun sendButton_isDisabledWhenFieldsAreEmpty() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Send").assertIsNotEnabled()
    }

    @Test
    fun recipientField_acceptsTextInput() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Enter number or name").performClick()
        composeTestRule.onNodeWithText("Enter number or name").performTextInput("555-1234")
        composeTestRule.onNodeWithText("555-1234").assertIsDisplayed()
    }

    @Test
    fun messageField_acceptsTextInput() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Type your message…").performClick()
        composeTestRule.onNodeWithText("Type your message…").performTextInput("Hello world")
        composeTestRule.onNodeWithText("Hello world").assertIsDisplayed()
    }

    @Test
    fun backButton_isDisplayed() {
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }

    @Test
    fun contactAutocomplete_showsMatchingContactOnInput() {
        viewModel = ContactViewModel(
            FakeContactRepository(
                phonebook = mapOf("+15551234" to "Alice")
            )
        )
        composeTestRule.setContent {
            NewMessageScreen(navController = rememberNavController(), contactViewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Enter number or name").performClick()
        composeTestRule.onNodeWithText("Enter number or name").performTextInput("Alice")
        composeTestRule.onNodeWithText("Alice (+15551234)", substring = true).assertIsDisplayed()
    }
}
