package com.shaeed.fcmclient.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CallLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CallLogDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.callLogDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun log(
        phoneNumber: String = "9876543210",
        normalizedNumber: String = "+919876543210",
        timestamp: Long = 1000L,
        status: String = "Incoming"
    ) = CallLog(
        phoneNumber = phoneNumber,
        normalizedNumber = normalizedNumber,
        timestamp = timestamp,
        status = status
    )

    @Test
    fun insert_persistedAndRetrievableViaFlow() = runTest {
        dao.insert(log())

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("9876543210", items[0].phoneNumber)
            assertEquals("Incoming", items[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAll_multipleEntries_orderedByTimestampDesc() = runTest {
        dao.insert(log(timestamp = 100L))
        dao.insert(log(timestamp = 300L))
        dao.insert(log(timestamp = 200L))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(300L, items[0].timestamp)
            assertEquals(200L, items[1].timestamp)
            assertEquals(100L, items[2].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteOlderThan_removesEntriesBeforeThreshold() = runTest {
        dao.insert(log(timestamp = 1000L))
        dao.insert(log(timestamp = 2000L))
        dao.insert(log(timestamp = 3000L))
        dao.deleteOlderThan(2500L)

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(3000L, items[0].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteOlderThan_noMatch_tableUnchanged() = runTest {
        dao.insert(log(timestamp = 5000L))
        dao.deleteOlderThan(1000L)

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById_removesCorrectEntry() = runTest {
        dao.insert(log(timestamp = 1000L))
        dao.insert(log(timestamp = 2000L))

        val allBefore = dao.getAll().first()
        val id1 = allBefore.first { it.timestamp == 1000L }.id.toLong()
        dao.deleteById(id1)

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(2000L, items[0].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById_nonExistentId_doesNotThrow() = runTest {
        dao.deleteById(999L)

        dao.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
