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
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

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
    fun getMessages_returnsSenderSpecificFlow() = runTest {
        val repo = FakeMessageRepository()
        repo.emit(listOf(msg("+91999"), msg("+91888", 2000L)))
        val vm = ConversationViewModel(repo)

        val messages = vm.getMessages("+91999")
        val job = launch { messages.collect {} }
        advanceUntilIdle()

        assertEquals(1, messages.value.size)
        assertEquals("+91999", messages.value[0].senderNormalized)
        job.cancel()
    }

    @Test
    fun sendMessage_recordedInFakeRepository() = runTest {
        val repo = FakeMessageRepository()
        val vm = ConversationViewModel(repo)

        vm.sendMessage("+91555", "hi there")
        advanceUntilIdle()

        assertEquals(1, repo.sentMessages.size)
        assertEquals(Pair("+91555", "hi there"), repo.sentMessages[0])
    }

    @Test
    fun markAsRead_callsRepositoryMarkAsRead() = runTest {
        val repo = FakeMessageRepository()
        val vm = ConversationViewModel(repo)

        vm.markAsRead("+91999")
        advanceUntilIdle()

        assertTrue(repo.readSenders.contains("+91999"))
    }

    @Test
    fun deleteMessage_removesMessageFromFakeRepository() = runTest {
        val repo = FakeMessageRepository()
        repo.emit(listOf(msg("+91999").copy(id = 42L)))
        val vm = ConversationViewModel(repo)

        vm.deleteMessage(42L)
        advanceUntilIdle()

        assertTrue(repo.deletedIds.contains(42L))
    }

    @Test
    fun getPagedMessages_returnsSameFlowForSameSender() = runTest {
        val repo = FakeMessageRepository()
        val vm = ConversationViewModel(repo)

        val flow1 = vm.getPagedMessages("+91999")
        val flow2 = vm.getPagedMessages("+91999")

        assertSame(flow1, flow2)
    }

    @Test
    fun getPagedMessages_returnsDifferentFlowForDifferentSender() = runTest {
        val repo = FakeMessageRepository()
        val vm = ConversationViewModel(repo)

        val flow1 = vm.getPagedMessages("+91999")
        val flow2 = vm.getPagedMessages("+91888")

        assertTrue(flow1 !== flow2)
    }
}
