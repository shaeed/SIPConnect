package com.shaeed.fcmclient.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaeed.fcmclient.data.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _phonebook = MutableStateFlow<Map<String, String>>(emptyMap())
    val phonebook: StateFlow<Map<String, String>> = _phonebook

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.loadIfNeeded()
            _phonebook.value = repository.getPhonebook()
            _isLoading.value = false
        }
    }

    fun getContactName(phoneNumber: String): String {
        return repository.getContactName(phoneNumber)
    }
}
