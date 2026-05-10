package com.shaeed.fcmclient.viewmodel

import com.shaeed.fcmclient.data.FakeContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_callsRepositoryLoadIfNeeded() = runTest {
        val repo = FakeContactRepository()
        ContactViewModel(repo)
        advanceUntilIdle()
        assertTrue(repo.loadCalled)
    }

    @Test
    fun phonebook_populatedAfterInit() = runTest {
        val contacts = mapOf("+919876543210" to "Alice", "+911234567890" to "Bob")
        val repo = FakeContactRepository(contacts)
        val vm = ContactViewModel(repo)

        assertFalse(vm.isLoading.value.not()) // isLoading starts true
        advanceUntilIdle()

        assertEquals(contacts, vm.phonebook.value)
    }

    @Test
    fun isLoading_falseAfterInitCompletes() = runTest {
        val repo = FakeContactRepository()
        val vm = ContactViewModel(repo)

        assertTrue(vm.isLoading.value)
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun getContactName_returnsNameFromRepository() = runTest {
        val repo = FakeContactRepository(mapOf("+919876543210" to "Alice"))
        val vm = ContactViewModel(repo)
        advanceUntilIdle()

        assertEquals("Alice", vm.getContactName("+919876543210"))
    }

    @Test
    fun getContactName_fallsBackToPhoneNumber() = runTest {
        val repo = FakeContactRepository(emptyMap())
        val vm = ContactViewModel(repo)
        advanceUntilIdle()

        assertEquals("+911111111111", vm.getContactName("+911111111111"))
    }
}
