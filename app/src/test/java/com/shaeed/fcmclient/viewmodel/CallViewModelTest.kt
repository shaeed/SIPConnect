package com.shaeed.fcmclient.viewmodel

import com.shaeed.fcmclient.data.CallLog
import com.shaeed.fcmclient.data.FakeCallLogRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun log(timestamp: Long) = CallLog(
        phoneNumber = "9876543210",
        normalizedNumber = "+919876543210",
        timestamp = timestamp,
        status = "Incoming"
    )

    @Test
    fun callLogs_emitsRepositoryData() = runTest {
        val logs = listOf(log(1000L), log(2000L))
        val repo = FakeCallLogRepository(logs)
        val vm = CallViewModel(repo)

        val job = launch { vm.callLogs.collect {} }
        advanceUntilIdle()

        assertEquals(logs, vm.callLogs.value)
        job.cancel()
    }

    @Test
    fun deleteOldCallLogs_passesThresholdApprox90DaysAgo() = runTest {
        val repo = FakeCallLogRepository()
        val vm = CallViewModel(repo)
        val before = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000

        val job = launch { vm.callLogs.collect {} }
        vm.deleteOldCallLogs()
        advanceUntilIdle()

        val after = System.currentTimeMillis() - 90L * 24 * 60 * 60 * 1000
        val threshold = repo.deletedThresholds.first()
        assertTrue(threshold in before..(after + 5000))
        job.cancel()
    }

    @Test
    fun deleteOldCallLogs_removesOldLogsFromFlow() = runTest {
        val ninetyOneDaysAgo = System.currentTimeMillis() - 91L * 24 * 60 * 60 * 1000
        val today = System.currentTimeMillis()
        val repo = FakeCallLogRepository(listOf(log(ninetyOneDaysAgo), log(today)))
        val vm = CallViewModel(repo)

        val job = launch { vm.callLogs.collect {} }
        vm.deleteOldCallLogs()
        advanceUntilIdle()

        assertEquals(1, vm.callLogs.value.size)
        assertEquals(today, vm.callLogs.value[0].timestamp)
        job.cancel()
    }
}
