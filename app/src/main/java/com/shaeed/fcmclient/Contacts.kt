package com.shaeed.fcmclient

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException

object ContactNameCache {
    private val map = mutableMapOf<String, String>()

    fun get(phoneNumber: String): String? {
        return map[phoneNumber]
    }

    fun put(phoneNumber: String, name: String) {
        map[phoneNumber] = name
    }
}

fun getContactName(context: Context, phoneNumber: String): String? {
    val cachedName = ContactNameCache.get(phoneNumber)
    val name = if (cachedName != null) {
        cachedName
    } else {
        val resolved = readPhoneBook(context, phoneNumber)
        if (resolved != null) {
            ContactNameCache.put(phoneNumber, resolved)
        }
        resolved
    }
    return name
}

@SuppressLint("Range")
fun readPhoneBook(context: Context, phoneNumber: String): String? {
    val normalizedIncoming = normalizeNumber(phoneNumber)
    val contacts = mutableListOf<Pair<String, String>>() // name to normalized number

    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null, null, null
    )

    cursor?.use {
        while (it.moveToNext()) {
            val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val normalizedContact = normalizeNumber(number)
            if (normalizedContact != null) {
                contacts.add(name to normalizedContact)
            }
        }
    }

    val match = contacts.find { it.second == normalizedIncoming }
    val name = match?.first
    return name
}

fun getContactNameSimple(context: Context, phoneNumber: String): String {
    val uri = Uri.withAppendedPath(
        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
        Uri.encode(phoneNumber)
    )

    val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null).use { cursor ->
        if (cursor != null && cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
            return cursor.getString(nameIndex)
        }
    }

    return phoneNumber // No match found
}

fun normalizeNumber(rawNumber: String, defaultRegion: String = "IN"): String? {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val numberProto = phoneUtil.parse(rawNumber, defaultRegion)
        phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164)
    } catch (e: NumberParseException) {
        null
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