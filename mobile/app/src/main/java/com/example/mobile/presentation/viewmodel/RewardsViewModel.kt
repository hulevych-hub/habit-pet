package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.InventoryItemEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val inventoryItemRepository: InventoryItemRepository,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    val outfits = inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT)
    val backgrounds = inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND)
    val auras = inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)

    val uiState: StateFlow<UiState> = combine(
        statisticsRepository.getStatistics(),
        petRepository.getPet()
    ) { statistics, pet ->
        UiState(
            globalStreak = statistics.currentStreak,
            globalStreakCompletedToday = statistics.lastStreakDate == getDayStart(System.currentTimeMillis()) / 86_400_000L,
            totalCoins = statistics.totalCoins,
            pet = pet
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = UiState(
            globalStreak = 0,
            globalStreakCompletedToday = false,
            totalCoins = 0,
            pet = PetEntity(id = 1)
        )
    )

    init {
        viewModelScope.launch {
            combine(outfits, backgrounds, auras) { _, _, _ -> }
                .collectLatest { _isLoading.value = false }
        }
    }

    fun clearError() {
        _error.value = null
    }

    suspend fun purchaseItem(itemId: Long): Int = try {
        _error.value = null
        inventoryItemRepository.purchaseItem(itemId)
    } catch (e: Exception) {
        _error.value = e.message ?: "Reward could not be claimed"
        -1
    }

    suspend fun equipItem(itemType: String, itemId: String): Int = try {
        _error.value = null
        petRepository.equipItem(itemType, itemId)
    } catch (e: Exception) {
        _error.value = e.message ?: "Reward could not be equipped"
        -1
    }

    suspend fun unequipItem(itemType: String): Int = try {
        _error.value = null
        petRepository.unequipItem(itemType)
    } catch (e: Exception) {
        _error.value = e.message ?: "Reward could not be unequipped"
        -1
    }

    data class UiState(
        val globalStreak: Int,
        val globalStreakCompletedToday: Boolean,
        val totalCoins: Int,
        val pet: PetEntity
    )

    private fun getDayStart(time: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = time
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
