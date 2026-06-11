package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.AchievementEngine
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.repository.AchievementRepository
import com.example.mobile.domain.repository.HabitRepository
import com.example.mobile.domain.repository.InventoryItemRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val achievementEngine: AchievementEngine,
    private val statisticsRepository: StatisticsRepository,
    private val habitRepository: HabitRepository,
    private val petRepository: PetRepository,
    private val inventoryItemRepository: InventoryItemRepository
) : ViewModel() {

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements

    val claimableAchievementCount: StateFlow<Int> = achievements
        .map { list -> list.count { achievement -> achievement.isUnlocked && !achievement.isClaimed } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val statistics: StateFlow<StatisticsEntity> = statisticsRepository.getStatistics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatisticsEntity()
        )

    val pet: StateFlow<PetEntity> = petRepository.getPet()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PetEntity(id = 1)
        )

    val habitCount: StateFlow<Int> = habitRepository.getAllHabits()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val ownedCustomizations: StateFlow<Int> = combine(
        inventoryItemRepository.getItemsByType(CustomizationTypes.OUTFIT),
        inventoryItemRepository.getItemsByType(CustomizationTypes.BACKGROUND),
        inventoryItemRepository.getItemsByType(CustomizationTypes.AURA)
    ) { outfits, backgrounds, auras ->
        outfits.count { it.isPurchased } +
            backgrounds.count { it.isPurchased } +
            auras.count { it.isPurchased }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

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

    fun claimAchievement(achievementId: Long) {
        viewModelScope.launch {
            achievementEngine.claimAchievement(achievementId)
        }
    }

    fun progressFor(
        achievement: AchievementEntity,
        stats: StatisticsEntity,
        petState: PetEntity,
        ownedCustomizationCount: Int,
        currentHabitCount: Int
    ): Int {
        if (achievement.isUnlocked) return achievement.targetValue

        return when (achievement.name) {
            "First Habit", "3 Habit Builder" -> currentHabitCount
            "7 Day Streak", "30 Day Streak" -> stats.currentStreak
            "100 Completions" -> stats.totalCompletions
            "1000 XP", "5000 XP" -> petState.xp.toInt()
            "Level 10", "Level 25" -> petState.level
            "First Customization", "Customization Collector" -> ownedCustomizationCount
            else -> 0
        }.coerceAtLeast(0)
    }

    fun progressFraction(current: Int, achievement: AchievementEntity): Float {
        val target = achievement.targetValue.coerceAtLeast(1)
        return current.coerceAtMost(target).toFloat() / target.toFloat()
    }

    fun rewardLabel(achievement: AchievementEntity): String {
        val rewards = mutableListOf<String>()

        if (achievement.rewardCoins > 0) {
            rewards.add("${achievement.rewardCoins} coins")
        }

        if (achievement.rewardExp > 0) {
            rewards.add("${achievement.rewardExp} EXP")
        }

        if (!achievement.rewardChestType.isNullOrBlank()) {
            val chestType = achievement.rewardChestType!!
            rewards.add("${chestType.substring(0, 1).uppercase()}${chestType.substring(1)} chest")
        }

        return if (rewards.isEmpty()) "No reward" else rewards.joinToString(", ")
    }
}
