package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.JournalEntryEntity
import com.example.mobile.data.local.dao.JournalEntryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for journal screen
 * Handles displaying and tracking journal entries
 */
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalEntryDao: JournalEntryDao
) : ViewModel() {

    // UI State
    private val _journalEntries = MutableStateFlow<List<JournalEntryEntity>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntryEntity>> = _journalEntries

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                journalEntryDao.getAllJournalEntries()
                    .collect { entries ->
                        _journalEntries.value = entries
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load journal entries: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Navigation handlers
    fun onNavigateUp() {
        // This would be handled by the NavController in the composable
    }
}