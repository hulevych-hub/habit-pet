package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            achievementRepository.getAllAchievements()
                .catch { e ->
                    _error.value = "Failed to load achievements: ${e.message}"
                    _isLoading.value = false
                }
                .collect { list ->
                    _achievements.value = list
                    _isLoading.value = false
                }
        }
    }
}