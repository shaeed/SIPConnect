package com.shaeed.fcmclient.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContactHelperTest {

    @After
    fun resetCache() {
        ContactHelper.phonebookCache = null
    }

    @Test
    fun normalizeNumber_e164Input_returnsSame() {
        assertEquals("+919876543210", ContactHelper.normalizeNumber("+919876543210"))
    }

    @Test
    fun normalizeNumber_indianNumberNoPrefix_addsCountryCode() {
        assertEquals("+919876543210", ContactHelper.normalizeNumber("9876543210"))
    }

    @Test
    fun normalizeNumber_usNumberWithRegionUS_returnsE164() {
        assertEquals("+12025551234", ContactHelper.normalizeNumber("2025551234", defaultRegion = "US"))
    }

    @Test
    fun normalizeNumber_invalidInput_returnsRaw() {
        assertEquals("not-a-number", ContactHelper.normalizeNumber("not-a-number"))
    }

    @Test
    fun normalizeNumber_emptyString_returnsEmpty() {
        assertEquals("", ContactHelper.normalizeNumber(""))
    }

    @Test
    fun normalizeNumber_numberWithSpaces_parsedCorrectly() {
        assertEquals("+919876543210", ContactHelper.normalizeNumber("98765 43210"))
    }

    @Test
    fun normalizeNumber_numberWithDashes_parsedCorrectly() {
        assertEquals("+919876543210", ContactHelper.normalizeNumber("98765-43210"))
    }

    @Test
    fun getContactNameFromCache_cacheNull_returnsNull() {
        ContactHelper.phonebookCache = null
        assertNull(ContactHelper.getContactNameFromCache("9876543210"))
    }

    @Test
    fun getContactNameFromCache_cacheHasEntry_returnsName() {
        ContactHelper.phonebookCache = mapOf("+919876543210" to "Alice")
        assertEquals("Alice", ContactHelper.getContactNameFromCache("9876543210"))
    }

    @Test
    fun getContactNameFromCache_entryMissing_returnsNull() {
        ContactHelper.phonebookCache = mapOf("+911111111111" to "Bob")
        assertNull(ContactHelper.getContactNameFromCache("9876543210"))
    }

    @Test
    fun getContactNameFromCache_emptyCache_returnsNull() {
        ContactHelper.phonebookCache = emptyMap()
        assertNull(ContactHelper.getContactNameFromCache("9876543210"))
    }
}
