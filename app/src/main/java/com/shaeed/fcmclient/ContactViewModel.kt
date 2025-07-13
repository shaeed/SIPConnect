package com.shaeed.fcmclient

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactViewModel(private val context: Context) : ViewModel() {

    private val _phonebook = MutableStateFlow<Map<String, String>>(emptyMap())
    val phonebook: StateFlow<Map<String, String>> = _phonebook

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            ContactHelper.loadCacheIfNeeded(context)
            _phonebook.value = ContactHelper.phonebookCache ?: emptyMap()
            _isLoading.value = false
        }
    }

    fun getContactName(phoneNumber: String): String {
        return ContactHelper.getContactNameFromCache(phoneNumber) ?: phoneNumber
    }
}
