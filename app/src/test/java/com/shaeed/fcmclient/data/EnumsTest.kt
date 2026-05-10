package com.shaeed.fcmclient.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnumsTest {

    @Test
    fun messageType_valuesCountIsThree() {
        assertEquals(3, MessageType.values().size)
    }

    @Test
    fun messageType_names_areStable() {
        assertEquals("INCOMING_GSM", MessageType.INCOMING_GSM.name)
        assertEquals("INCOMING_FIREBASE", MessageType.INCOMING_FIREBASE.name)
        assertEquals("OUTGOING", MessageType.OUTGOING.name)
    }

    @Test
    fun deliveryStatus_valuesCountIsFour() {
        assertEquals(4, DeliveryStatus.values().size)
    }

    @Test
    fun deliveryStatus_names_areStable() {
        assertEquals("PENDING", DeliveryStatus.PENDING.name)
        assertEquals("SENT", DeliveryStatus.SENT.name)
        assertEquals("DELIVERED", DeliveryStatus.DELIVERED.name)
        assertEquals("FAILED", DeliveryStatus.FAILED.name)
    }

    @Test
    fun prefKeys_allNonEmptyAndUnique() {
        val keys = listOf(
            PrefKeys.IP_ADDRESS,
            PrefKeys.LAST_FCM_TOKEN,
            PrefKeys.SIP_SERVER_USER,
            PrefKeys.SIP_SERVER_PASS,
            PrefKeys.SIP_SERVER_USER2,
            PrefKeys.SIP_SERVER_PASS2,
            PrefKeys.REGISTRATION_STATUS,
            PrefKeys.APP_MODE
        )
        keys.forEach { assertTrue("Key must not be blank: $it", it.isNotBlank()) }
        assertEquals("All PrefKeys must be unique", keys.size, keys.toSet().size)
    }

    @Test
    fun perfValues_yesAndNoAreDistinct() {
        assertNotEquals(PerfValues.YES, PerfValues.NO)
    }

    @Test
    fun appMode_allFourModesNonEmptyAndDistinct() {
        val modes = listOf(AppMode.SERVER, AppMode.CLIENT, AppMode.SMS_MANAGER, AppMode.NORMAL)
        modes.forEach { assertTrue("Mode must not be blank: $it", it.isNotBlank()) }
        assertEquals("All AppMode values must be unique", modes.size, modes.toSet().size)
    }
}
