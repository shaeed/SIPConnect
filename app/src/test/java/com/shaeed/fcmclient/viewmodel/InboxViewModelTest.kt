package com.shaeed.fcmclient.viewmodel

import com.shaeed.fcmclient.data.FakeMessageRepository
import com.shaeed.fcmclient.data.MessageEntity
import com.shaeed.fcmclient.data.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun msg(sender: String, timestamp: Long = 1000L) = MessageEntity(
        threadId = 1L,
        sender = sender,
        senderNormalized = sender,
        body = "hello",
        timestamp = timestamp,
        type = MessageType.INCOMING_GSM
    )

    @Test
    fun conversations_emitsConversationListFromRepository() = runTest {
        val repo = FakeMessageRepository()
        repo.emit(listOf(msg("+91999"), msg("+91888", 2000L)))
        val vm = InboxViewModel(repo)

        val job = launch { vm.conversations.collect {} }
        advanceUntilIdle()

        assertEquals(2, vm.conversations.value.size)
        job.cancel()
    }

    @Test
    fun deleteMessages_delegatesToRepository() = runTest {
        val repo = FakeMessageRepository()
        repo.emit(listOf(msg("+91999"), msg("+91888", 2000L)))
        val vm = InboxViewModel(repo)

        val job = launch { vm.conversations.collect {} }
        vm.deleteMessages("+91999")
        advanceUntilIdle()

        assertFalse(repo.deletedSenders.isEmpty())
        assertEquals("+91999", repo.deletedSenders[0])
        assertEquals(1, vm.conversations.value.size)
        assertEquals("+91888", vm.conversations.value[0].senderNormalized)
        job.cancel()
    }

    @Test
    fun deleteMessages_doesNotAffectOtherSenders() = runTest {
        val repo = FakeMessageRepository()
        repo.emit(listOf(msg("+91999"), msg("+91888", 2000L)))
        val vm = InboxViewModel(repo)

        val job = launch { vm.conversations.collect {} }
        vm.deleteMessages("+91999")
        advanceUntilIdle()

        assertEquals(1, vm.conversations.value.size)
        assertEquals("+91888", vm.conversations.value[0].senderNormalized)
        job.cancel()
    }
}
