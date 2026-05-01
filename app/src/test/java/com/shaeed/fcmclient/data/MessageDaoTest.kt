package com.shaeed.fcmclient.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MessageDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MessageDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.messageDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun msg(
        sender: String = "+91999",
        normalized: String = "+91999",
        body: String = "hello",
        timestamp: Long = 1000L,
        threadId: Long = 1L,
        type: MessageType = MessageType.INCOMING_GSM,
        read: Boolean = false,
        deleted: Boolean = false,
        starred: Boolean = false,
        deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING
    ) = MessageEntity(
        threadId = threadId,
        sender = sender,
        senderNormalized = normalized,
        body = body,
        timestamp = timestamp,
        type = type,
        read = read,
        deleted = deleted,
        starred = starred,
        deliveryStatus = deliveryStatus
    )

    @Test
    fun insert_assignsAutoIncrementId() = runTest {
        val id = dao.insert(msg())
        assertTrue(id > 0)
    }

    @Test
    fun insert_multipleMessages_distinctIds() = runTest {
        val id1 = dao.insert(msg(timestamp = 1000L))
        val id2 = dao.insert(msg(timestamp = 2000L))
        val id3 = dao.insert(msg(timestamp = 3000L))
        assertEquals(3, setOf(id1, id2, id3).size)
    }

    @Test
    fun getMessagesForSender_emitsOnlyMatchingSender() = runTest {
        dao.insert(msg(normalized = "+91999", timestamp = 1000L))
        dao.insert(msg(normalized = "+91999", timestamp = 2000L))
        dao.insert(msg(normalized = "+91888", timestamp = 3000L))

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.senderNormalized == "+91999" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMessagesForSender_orderedByTimestampAsc() = runTest {
        dao.insert(msg(normalized = "+91999", timestamp = 200L))
        dao.insert(msg(normalized = "+91999", timestamp = 100L))

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertEquals(100L, items[0].timestamp)
            assertEquals(200L, items[1].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getConversationList_groupsBySender_returnsLatestTimestampPerGroup() = runTest {
        dao.insert(msg(normalized = "+91999", timestamp = 1000L))
        dao.insert(msg(normalized = "+91999", timestamp = 3000L))
        dao.insert(msg(normalized = "+91888", timestamp = 2000L))

        dao.getConversationList().test {
            val items = awaitItem()
            assertEquals(2, items.size)
            val senderMap = items.associateBy { it.senderNormalized }
            assertEquals(3000L, senderMap["+91999"]!!.timestamp)
            assertEquals(2000L, senderMap["+91888"]!!.timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun markAsRead_setsReadFlagForMatchingSender() = runTest {
        dao.insert(msg(normalized = "+91999", read = false, timestamp = 1000L))
        dao.insert(msg(normalized = "+91999", read = false, timestamp = 2000L))
        dao.markAsRead("+91999")

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertTrue(items.all { it.read })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun markAsRead_doesNotAffectOtherSenders() = runTest {
        dao.insert(msg(normalized = "+91999", read = false))
        dao.insert(msg(normalized = "+91888", read = false, timestamp = 2000L))
        dao.markAsRead("+91999")

        dao.getMessagesForSender("+91888").test {
            val items = awaitItem()
            assertFalse(items[0].read)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteMessages_removesAllForSender() = runTest {
        dao.insert(msg(normalized = "+91999", timestamp = 1000L))
        dao.insert(msg(normalized = "+91999", timestamp = 2000L))
        dao.deleteMessages("+91999")

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteMessage_removesById_leavesOthersIntact() = runTest {
        val id1 = dao.insert(msg(normalized = "+91999", timestamp = 1000L))
        val id2 = dao.insert(msg(normalized = "+91999", timestamp = 2000L))
        dao.deleteMessage(id1)

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(id2, items[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun starMessage_setsStarred() = runTest {
        val id = dao.insert(msg(starred = false))
        dao.starMessage(id, true)

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertTrue(items[0].starred)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun starMessage_unsetsStarred() = runTest {
        val id = dao.insert(msg(starred = true))
        dao.starMessage(id, false)

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertFalse(items[0].starred)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateDeliveryStatus_changesStatus() = runTest {
        val id = dao.insert(msg(deliveryStatus = DeliveryStatus.PENDING))
        dao.updateDeliveryStatus(id, DeliveryStatus.DELIVERED)

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertEquals(DeliveryStatus.DELIVERED, items[0].deliveryStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMessagesForThread_returnsOnlyMatchingThread() = runTest {
        dao.insert(msg(normalized = "+91999", threadId = 1L, timestamp = 1000L))
        dao.insert(msg(normalized = "+91999", threadId = 1L, timestamp = 2000L))
        dao.insert(msg(normalized = "+91888", threadId = 2L, timestamp = 3000L))

        dao.getMessagesForThread(1L).test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertTrue(items.all { it.threadId == 1L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun markThreadAsRead_setsReadOnAllInThread() = runTest {
        dao.insert(msg(normalized = "+91999", threadId = 1L, read = false, timestamp = 1000L))
        dao.insert(msg(normalized = "+91999", threadId = 1L, read = false, timestamp = 2000L))
        dao.markThreadAsRead(1L)

        dao.getMessagesForThread(1L).test {
            val items = awaitItem()
            assertTrue(items.all { it.read })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getThreads_excludesDeletedMessages() = runTest {
        dao.insert(msg(normalized = "+91999", threadId = 1L, deleted = false))
        dao.insert(msg(normalized = "+91888", threadId = 2L, deleted = true, timestamp = 2000L))

        dao.getThreads().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(1L, items[0].threadId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun purgeDeletedMessages_removesDeletedRows() = runTest {
        dao.insert(msg(normalized = "+91999", threadId = 1L, deleted = true))
        dao.insert(msg(normalized = "+91888", threadId = 2L, deleted = false, timestamp = 2000L))
        dao.purgeDeletedMessages()

        dao.getConversationList().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("+91888", items[0].senderNormalized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun update_modifiesExistingEntity() = runTest {
        dao.insert(msg(body = "original"))
        val inserted = dao.getMessagesForSender("+91999").first()[0]
        dao.update(inserted.copy(body = "updated"))

        dao.getMessagesForSender("+91999").test {
            val items = awaitItem()
            assertEquals("updated", items[0].body)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
