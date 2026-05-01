package com.shaeed.fcmclient.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RESTDataTest {

    @Test
    fun registerDevice_fieldsStoredCorrectly() {
        val r = RegisterDevice("device1", "user1", "token1")
        assertEquals("device1", r.device_id)
        assertEquals("user1", r.username)
        assertEquals("token1", r.fcm_token)
    }

    @Test
    fun registerDevice_equalityBetweenIdenticalInstances() {
        assertEquals(RegisterDevice("a", "b", "c"), RegisterDevice("a", "b", "c"))
    }

    @Test
    fun registerDevice_copyModifiesOnlyTargetField() {
        val original = RegisterDevice("d", "u", "t")
        val copy = original.copy(fcm_token = "new")
        assertEquals("d", copy.device_id)
        assertEquals("u", copy.username)
        assertEquals("new", copy.fcm_token)
    }

    @Test
    fun smsAlert_fieldsStoredCorrectly() {
        val s = SmsAlert("user", "1234567890", "hello", "device")
        assertEquals("user", s.username)
        assertEquals("1234567890", s.phone_number)
        assertEquals("hello", s.body)
        assertEquals("device", s.device_id)
    }

    @Test
    fun postResponse_fieldStoredCorrectly() {
        val p = PostResponse("ok")
        assertEquals("ok", p.message)
    }

    @Test
    fun postResponse_emptyMessageAllowed() {
        val p = PostResponse("")
        assertEquals("", p.message)
    }

    @Test
    fun restartSip_fieldsStoredCorrectly() {
        val r = RestartSip("device", "user")
        assertEquals("device", r.device_id)
        assertEquals("user", r.username)
    }

    @Test
    fun tokenResponse_fieldStoredCorrectly() {
        val t = TokenResponse("mytoken")
        assertEquals("mytoken", t.fcm_token)
    }

    @Test
    fun postResponse_differentMessagesNotEqual() {
        assertNotEquals(PostResponse("a"), PostResponse("b"))
    }
}
