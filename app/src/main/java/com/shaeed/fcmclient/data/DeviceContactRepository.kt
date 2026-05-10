package com.shaeed.fcmclient.data

import android.content.Context
import com.shaeed.fcmclient.util.ContactHelper

class DeviceContactRepository(private val context: Context) : ContactRepository {
    override suspend fun loadIfNeeded() = ContactHelper.loadCacheIfNeeded(context)
    override fun getPhonebook(): Map<String, String> = ContactHelper.phonebookCache ?: emptyMap()
    override fun getContactName(phoneNumber: String): String = ContactHelper.getContactNameFromCache(phoneNumber) ?: phoneNumber
}
