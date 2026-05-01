package com.shaeed.fcmclient.data

interface ContactRepository {
    suspend fun loadIfNeeded()
    fun getPhonebook(): Map<String, String>
    fun getContactName(phoneNumber: String): String
}
