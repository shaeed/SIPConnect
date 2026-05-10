package com.shaeed.fcmclient.data

class FakeContactRepository(
    private val phonebook: Map<String, String> = emptyMap()
) : ContactRepository {

    override suspend fun loadIfNeeded() {}

    override fun getPhonebook(): Map<String, String> = phonebook

    override fun getContactName(phoneNumber: String): String = phonebook[phoneNumber] ?: phoneNumber
}
