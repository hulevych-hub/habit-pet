package com.example.mobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.AchievementEngine
import com.example.mobile.domain.AchievementProgressSource
import com.example.mobile.domain.AchievementReward
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.EquipableConfig
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
        .map { list ->
            list.count { achievement ->
                val definition = AchievementsConfig.achievementById(achievement.id)
                val targetReached = definition?.targetValue?.let { achievement.progress >= it } ?: achievement.isUnlocked
                achievement.isUnlocked && !achievement.isClaimed && targetReached
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isClaiming = MutableStateFlow(false)
    val isClaiming: StateFlow<Boolean> = _isClaiming

    fun clearError() {
        _error.value = null
    }

    fun retryLoadAchievements() {
        loadAchievements()
    }

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

    private val bestStreak: StateFlow<Int> = statistics.map { it.bestStreak }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val bestCombo: StateFlow<Int> = statistics.map { it.bestCombo }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val daysActive: StateFlow<Int> = statistics.map { it.daysActive }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val petAgeDays: StateFlow<Int> = statistics.map { it.petAgeDays }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val totalCoins: StateFlow<Int> = statistics.map { it.totalCoins }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
                    _achievements.value = list.sortedWith(
                        compareBy<AchievementEntity> { achievement ->
                            AchievementsConfig.achievementById(achievement.id)?.difficultyRank ?: Int.MAX_VALUE
                        }.thenByDescending { it.isUnlocked }
                            .thenByDescending { it.isClaimed }
                    )
                    _isLoading.value = false
                }
        }
    }

    fun claimAchievement(achievementId: String) {
        viewModelScope.launch {
            val processed = claimAchievementId(achievementId)
            if (!processed) {
                _error.value = "This achievement reward is no longer available"
            }
        }
    }

    fun claimAllAchievements() {
        viewModelScope.launch {
            if (_isClaiming.value) return@launch

            _isClaiming.value = true
            _error.value = null
            val claimableIds = _achievements.value
                .filter { achievement ->
                    val definition = AchievementsConfig.achievementById(achievement.id)
                    val targetReached = definition?.targetValue?.let { achievement.progress >= it } ?: achievement.isUnlocked
                    achievement.isUnlocked && !achievement.isClaimed && targetReached
                }
                .map { it.id }

            try {
                var processedAny = false
                for (achievementId in claimableIds) {
                    val processed = claimAchievementId(achievementId)
                    if (processed) {
                        processedAny = true
                    } else {
                        _error.value = "Some achievement rewards were not available"
                        break
                    }
                }
                if (!processedAny && claimableIds.isNotEmpty()) {
                    _error.value = "No achievement rewards were available"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Achievement rewards could not be claimed"
            } finally {
                _isClaiming.value = false
            }
        }
    }

    private suspend fun claimAchievementId(achievementId: String): Boolean = try {
        _error.value = null
        achievementEngine.claimAchievement(achievementId)
    } catch (e: Exception) {
        _error.value = e.message ?: "Achievement could not be claimed"
        false
    }

    fun progressFor(
        achievement: AchievementEntity,
        stats: StatisticsEntity,
        petState: PetEntity,
        ownedCustomizationCount: Int,
        currentHabitCount: Int
    ): Int {
        val definition = AchievementsConfig.achievementById(achievement.id)
        if (achievement.isUnlocked && definition?.targetValue != null) {
            return definition.targetValue
        }

        return when (definition?.progressSource) {
            AchievementProgressSource.HABIT_COUNT -> currentHabitCount
            AchievementProgressSource.CURRENT_STREAK -> stats.currentStreak
            AchievementProgressSource.TOTAL_COMPLETIONS -> stats.totalCompletions
            AchievementProgressSource.TOTAL_XP -> petState.xp.toInt().coerceAtMost(Int.MAX_VALUE)
            AchievementProgressSource.PET_LEVEL -> petState.level
            AchievementProgressSource.OWNED_CUSTOMIZATIONS -> ownedCustomizationCount
            AchievementProgressSource.BEST_STREAK -> stats.bestStreak
            AchievementProgressSource.BEST_COMBO -> stats.bestCombo
            AchievementProgressSource.DAYS_ACTIVE -> stats.daysActive
            AchievementProgressSource.PET_AGE_DAYS -> stats.petAgeDays
            AchievementProgressSource.TOTAL_COINS -> stats.totalCoins
            AchievementProgressSource.FREEZES_USED -> StatisticsEntity.parseFreezeDates(stats.streakFreezeDatesJson).size
            AchievementProgressSource.CHALLENGES_COMPLETED,
            AchievementProgressSource.CHESTS_OPENED,
            AchievementProgressSource.DAILY_LOGINS,
            AchievementProgressSource.EVOLUTIONS,
            AchievementProgressSource.ACHIEVEMENTS_CLAIMED -> achievement.progress
            null -> 0
        }.coerceAtLeast(0)
    }

    fun progressFraction(current: Int, achievement: AchievementEntity): Float {
        val target = AchievementsConfig.achievementById(achievement.id)?.targetValue
        return if (target == null) {
            if (achievement.isUnlocked) 1f else 0f
        } else {
            current.coerceAtMost(target).toFloat() / target.toFloat()
        }
    }

    fun progressLabel(current: Int, achievement: AchievementEntity): String {
        val target = AchievementsConfig.achievementById(achievement.id)?.targetValue
        return if (target == null) {
            "Instant reward"
        } else {
            "$current / $target"
        }
    }

    fun rewardLabels(achievement: AchievementEntity): List<String> {
        val rewards = AchievementsConfig.achievementById(achievement.id)?.rewards.orEmpty()
        return if (rewards.isEmpty()) {
            listOf("No reward")
        } else {
            rewards.map { it.rewardLabel() }
        }
    }

    private fun AchievementReward.rewardLabel(): String = when (this) {
        is AchievementReward.CoinReward -> "+$amount coins"
        is AchievementReward.ExpReward -> "+$amount EXP"
        is AchievementReward.ChestReward -> "${chestType.name.replaceFirstChar { it.uppercase() }} chest"
        is AchievementReward.CustomizationReward ->
            EquipableConfig.definition(equipableId)?.name ?: CustomizationTypes.displayName(type)
    }
}
