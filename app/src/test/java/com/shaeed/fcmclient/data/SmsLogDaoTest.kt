package com.shaeed.fcmclient.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
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
class SmsLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SmsLogDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.smsLogDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insert_persistedCorrectly() = runTest {
        dao.insert(SmsLog(from = "Alice", body = "hello", timestamp = 1000L))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Alice", items[0].from)
            assertEquals("hello", items[0].body)
            assertEquals(1000L, items[0].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAll_multipleEntries_orderedByTimestampDesc() = runTest {
        dao.insert(SmsLog(from = "A", body = "msg1", timestamp = 100L))
        dao.insert(SmsLog(from = "B", body = "msg2", timestamp = 500L))
        dao.insert(SmsLog(from = "C", body = "msg3", timestamp = 300L))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(500L, items[0].timestamp)
            assertEquals(300L, items[1].timestamp)
            assertEquals(100L, items[2].timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAll_emptyTable_emitsEmptyList() = runTest {
        dao.getAll().test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
