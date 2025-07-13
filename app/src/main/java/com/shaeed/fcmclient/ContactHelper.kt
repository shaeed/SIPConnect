package com.shaeed.fcmclient

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ContactHelper {

    // Mutex to protect cache loading
    private val mutex = Mutex()

    // Cached phonebook map: normalized phoneNumber -> contactName
    @Volatile
    var phonebookCache: Map<String, String>? = null

    fun normalizeNumber(rawNumber: String, defaultRegion: String = "IN"): String? {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val numberProto = phoneUtil.parse(rawNumber, defaultRegion)
            phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            null
        }
    }

    // Load all contacts into cache if not loaded
    suspend fun loadCacheIfNeeded(context: Context) {
        if (phonebookCache == null) {
            mutex.withLock {
                if (phonebookCache == null) {
                    phonebookCache = withContext(Dispatchers.IO) {
                        loadAllContacts(context)
                    }
                }
            }
        }
    }

    // Query all contacts from device
    private fun loadAllContacts(context: Context): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val rawNumber = cursor.getString(numberIndex)
                val normalized = normalizeNumber(rawNumber)
                if (normalized != null) {
                    map[normalized] = name
                }
            }
        }
        return map
    }

    // Get contact name for a phone number from cache (loads cache if needed)
    suspend fun getContactName(context: Context, phoneNumber: String): String {
        loadCacheIfNeeded(context)  // make sure cache is loaded

        val normalized = normalizeNumber(phoneNumber)
        return phonebookCache?.get(normalized) ?: phoneNumber
    }

    // Immediate lookup from cache, no loading, returns null if cache not ready
    fun getContactNameFromCache(phoneNumber: String): String? {
        val normalized = normalizeNumber(phoneNumber)
        return phonebookCache?.get(normalized)
    }
}


fun requestContactPermission(activity: ComponentActivity){
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            1001
        )
    }
}
