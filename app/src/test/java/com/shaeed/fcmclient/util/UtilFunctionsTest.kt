package com.shaeed.fcmclient.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilFunctionsTest {

    // ── isoToMillis ──────────────────────────────────────────────────────────

    @Test
    fun isoToMillis_validIso8601_returnsCorrectMillis() {
        assertEquals(1705314600000L, UtilFunctions.isoToMillis("2024-01-15T10:30:00Z"))
    }

    @Test
    fun isoToMillis_invalidString_returnsFallbackNearCurrentTime() {
        val before = System.currentTimeMillis()
        val result = UtilFunctions.isoToMillis("not-a-date")
        val after = System.currentTimeMillis()
        assertTrue(result in before..after)
    }

    // ── extractOtp ───────────────────────────────────────────────────────────

    @Test
    fun extractOtp_otpKeyword_returnsDigits() {
        assertEquals("123456", UtilFunctions.extractOtp("Your OTP is 123456"))
    }

    @Test
    fun extractOtp_verificationKeyword_returnsDigits() {
        assertEquals("9876", UtilFunctions.extractOtp("Use verification code 9876 to proceed"))
    }

    @Test
    fun extractOtp_authCodeKeyword_returnsDigits() {
        assertEquals("12345678", UtilFunctions.extractOtp("Your auth code is 12345678"))
    }

    @Test
    fun extractOtp_passcodeKeyword_returnsDigits() {
        assertEquals("4321", UtilFunctions.extractOtp("Enter passcode 4321 to continue"))
    }

    @Test
    fun extractOtp_noKeyword_returnsNull() {
        assertNull(UtilFunctions.extractOtp("Your package has shipped"))
    }

    @Test
    fun extractOtp_keywordWithNoDigits_returnsNull() {
        assertNull(UtilFunctions.extractOtp("Please verify your account"))
    }

    @Test
    fun extractOtp_threeDigitNumber_returnsNull() {
        // "123" is only 3 digits — below the 4-digit minimum
        assertNull(UtilFunctions.extractOtp("Your OTP area code is 123"))
    }

    @Test
    fun extractOtp_fourDigitOtp_returnsDigits() {
        assertEquals("5432", UtilFunctions.extractOtp("OTP: 5432"))
    }

    @Test
    fun extractOtp_eightDigitOtp_returnsDigits() {
        assertEquals("12345678", UtilFunctions.extractOtp("Your OTP is 12345678"))
    }

    @Test
    fun extractOtp_nineDigitNumber_returnsNull() {
        // 9 digits exceeds the 4-8 digit range
        assertNull(UtilFunctions.extractOtp("Your OTP is 123456789"))
    }

    @Test
    fun extractOtp_caseInsensitiveKeyword_returnsDigits() {
        assertNotNull(UtilFunctions.extractOtp("your otp is 1234"))
        assertNotNull(UtilFunctions.extractOtp("YOUR OTP IS 1234"))
    }

    // ── formatOtp ────────────────────────────────────────────────────────────

    @Test
    fun formatOtp_4digits_returnsUnchanged() {
        assertEquals("1234", UtilFunctions.formatOtp("1234"))
    }

    @Test
    fun formatOtp_5digits_returnsThreeAndTwo() {
        assertEquals("123 45", UtilFunctions.formatOtp("12345"))
    }

    @Test
    fun formatOtp_6digits_returnsTwoGroupsOfThree() {
        assertEquals("123 456", UtilFunctions.formatOtp("123456"))
    }

    @Test
    fun formatOtp_7digits_returnsFourAndThree() {
        assertEquals("1234 567", UtilFunctions.formatOtp("1234567"))
    }

    @Test
    fun formatOtp_8digits_returnsTwoGroupsOfFour() {
        assertEquals("1234 5678", UtilFunctions.formatOtp("12345678"))
    }
}
