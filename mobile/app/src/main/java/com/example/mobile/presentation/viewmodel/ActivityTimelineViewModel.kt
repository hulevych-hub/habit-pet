package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.domain.repository.GameEventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityTimelineViewModel @Inject constructor(
    private val gameEventRepository: GameEventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<GameEventEntity>>(emptyList())
    val events: StateFlow<List<GameEventEntity>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var offset = 0
    private var loadedAll = false

    init {
        loadMore()
    }

    fun loadMore() {
        if (loadedAll || _isLoading.value || _isLoadingMore.value) return

        viewModelScope.launch {
            val isFirstPage = offset == 0
            if (isFirstPage) {
                _isLoading.value = true
            } else {
                _isLoadingMore.value = true
            }
            _error.value = null

            try {
                val page = gameEventRepository.getRecentEvents(limit = DEFAULT_LIMIT, offset = offset)
                    .firstOrNull()
                    .orEmpty()

                if (page.isEmpty()) {
                    loadedAll = true
                    _hasMore.value = false
                    return@launch
                }

                val existingIds = _events.value.map { it.id }.toSet()
                val newEvents = page.filter { it.id !in existingIds }

                _events.value = (_events.value + newEvents)
                    .sortedByDescending { it.timestamp }

                offset += page.size
                loadedAll = page.size < DEFAULT_LIMIT
                _hasMore.value = !loadedAll
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 100
    }
}
