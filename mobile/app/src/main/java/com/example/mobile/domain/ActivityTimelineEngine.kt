package com.example.mobile.domain

import android.content.Context
import android.util.Log
import com.example.mobile.domain.repository.ChallengeRepository
import com.example.mobile.domain.repository.GameEventRepository
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.domain.ChallengeRewardDefinition
import com.example.mobile.presentation.ui.events.RewardUiEvent
import com.example.mobile.presentation.ui.reward.RewardEventBus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityTimelineEngine @Inject constructor(
    private val gameEventRepository: GameEventRepository,
    private val rewardEventBus: RewardEventBus,
    private val petRepository: PetRepository,
    private val statisticsRepository: StatisticsRepository,
    private val challengeRepository: ChallengeRepository,
    @ApplicationContext private val context: Context
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var started = false

    fun start() {
        if (started) return
        started = true

        observeRewardEvents()
        ensureFirstDailyLoginEvent()
    }

    fun logHabitCompleted(
        habitName: String,
        xpEarned: Long,
        coinsEarned: Int,
        combo: Int = 0,
        comboBonusXp: Long = 0L,
        comboMultiplier: Float = 1f
    ) {
        scope.launch {
            gameEventRepository.logEvent(
                GameEventFactory.habitCompleted(
                    habitName = habitName,
                    xpEarned = xpEarned,
                    coinsEarned = coinsEarned,
                    combo = combo,
                    comboBonusXp = comboBonusXp,
                    comboMultiplier = comboMultiplier
                )
            )
        }
    }

    fun logComboMilestone(combo: Int, bonusXp: Long, multiplier: Float) {
        scope.launch {
            gameEventRepository.logEvent(
                GameEventFactory.comboMilestone(
                    combo = combo,
                    bonusXp = bonusXp,
                    multiplier = multiplier
                )
            )
        }
    }

    fun logAchievementUnlocked(achievementName: String) {
        scope.launch {
            gameEventRepository.logEvent(GameEventFactory.achievementUnlocked(achievementName))
        }
    }

    fun logLevelUp(level: Int, coins: Int) {
        scope.launch {
            gameEventRepository.logEvent(GameEventFactory.levelUp(level, coins))
        }
    }

    fun logDragonEvolution(fromStage: Int, toStage: Int) {
        scope.launch {
            gameEventRepository.logEvent(
                GameEventFactory.dragonEvolution(
                    fromStage = fromStage,
                    toStage = toStage,
                    fromStageName = ExpConfig.evolutionStageName(fromStage),
                    toStageName = ExpConfig.evolutionStageName(toStage)
                )
            )
        }
    }

    fun logEvolutionMilestoneNearing(toStage: Int, xp: Long) {
        scope.launch {
            val prefsKey = "${KEY_EVOLUTION_MILESTONE_NEARING}_$toStage"
            if (prefs.getBoolean(prefsKey, false)) return@launch

            val currentStage = ExpConfig.calculateEvolutionStageFromXp(xp)
            val nextStage = (currentStage + 1).coerceAtMost(ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex)
            if (nextStage != toStage) return@launch

            val progress = evolutionProgressFraction(xp, currentStage)
            if (progress < EVOLUTION_MILESTONE_NEARING_PROGRESS) return@launch

            gameEventRepository.logEvent(
                GameEventFactory.evolutionMilestoneNearing(
                    toStage = toStage,
                    xp = xp,
                    progress = progress
                )
            )
            prefs.edit().putBoolean(prefsKey, true).apply()
        }
    }

    fun logStreakMilestone(streak: Int, chestType: ChestType) {
        scope.launch {
            gameEventRepository.logEvent(
                GameEventFactory.streakMilestone(
                    streak = streak,
                    chestType = chestType.name
                )
            )
        }
    }

    fun logSurpriseReward(
        coins: Int,
        xp: Long,
        chestType: String,
        hasCustomization: Boolean
    ) {
        scope.launch {
            gameEventRepository.logEvent(
                GameEventFactory.surpriseReward(
                    coins = coins,
                    xp = xp,
                    chestType = chestType,
                    hasCustomization = hasCustomization
                )
            )
        }
    }

    fun logChallengeCompleted(challengeName: String, rewards: List<ChallengeRewardDefinition>) {
        scope.launch {
            gameEventRepository.logEvent(
                GameEventFactory.challengeCompleted(
                    challengeName = challengeName,
                    rewardSummary = rewards.map { it.rewardLabel() }
                )
            )
        }
    }

    private fun observeRewardEvents() {
        scope.launch {
            rewardEventBus.rewardEvents.collect { event ->
                if (event is RewardUiEvent.ChestReward) {
                    val coins = (event.amount as? Int) ?: 0
                    val chestType = event.rewardType.split('_').lastOrNull() ?: "normal"
                    challengeRepository.recordChestOpened()
                    gameEventRepository.logEvent(
                        GameEventFactory.chestOpened(
                            rewardType = event.rewardType,
                            chestType = chestType,
                            coins = coins,
                            expAmount = event.expAmount,
                            hasCustomization = event.customizationId != null
                        )
                    )
                }
            }
        }
    }

    private fun ensureFirstDailyLoginEvent() {
        scope.launch {
            val now = System.currentTimeMillis()
            val today = dayKey(now)
            val lastLoginDay = prefs.getString(KEY_LAST_LOGIN_DAY, null)
            val lastActiveTimestamp = prefs.getLong(KEY_LAST_ACTIVE_SESSION_TIMESTAMP, 0L)
            val lastSessionDifference = formatLastSessionDifference(lastActiveTimestamp)
            val streak = statisticsRepository.getStatistics().firstOrNull()?.currentStreak ?: 0
            val motivationalMessage = motivationalMessageFor(lastActiveTimestamp)

            if (lastLoginDay != today) {
                gameEventRepository.logEvent(
                    GameEventFactory.firstDailyLogin(
                        streak = streak,
                        lastActiveTimestamp = lastActiveTimestamp,
                        lastSessionDifference = lastSessionDifference,
                        motivationalMessage = motivationalMessage
                    )
                )

                prefs.edit()
                    .putString(KEY_LAST_LOGIN_DAY, today)
                    .apply()
            }

            prefs.edit()
                .putLong(KEY_LAST_ACTIVE_SESSION_TIMESTAMP, now)
                .apply()
        }
    }

    private fun dayKey(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return "${calendar.get(java.util.Calendar.YEAR)}-${calendar.get(java.util.Calendar.MONTH) + 1}-${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }

    private fun evolutionProgressFraction(totalXp: Long, stage: Int): Float {
        val lastIndex = ExpConfig.EVOLUTION_STAGE_NAMES.lastIndex
        val currentStage = stage.coerceIn(0, lastIndex)
        val nextStage = (currentStage + 1).coerceAtMost(lastIndex)
        val currentThreshold = ExpConfig.xpThresholdForStage(currentStage)
        val nextThreshold = ExpConfig.xpThresholdForStage(nextStage)

        if (nextStage == currentStage || nextThreshold <= currentThreshold) return 1f

        val progress = (totalXp - currentThreshold).coerceAtLeast(0L)
        return (progress.toFloat() / (nextThreshold - currentThreshold).toFloat()).coerceIn(0f, 1f)
    }

    private fun formatLastSessionDifference(lastActiveTimestamp: Long): String {
        if (lastActiveTimestamp <= 0L) {
            return "This is your first visit."
        }

        val diff = (System.currentTimeMillis() - lastActiveTimestamp).coerceAtLeast(0L)
        val minute = 60_000L
        val hour = 60L * minute
        val day = 24L * hour

        return when {
            diff < minute -> "Last session: just now."
            diff < hour -> "Last session: ${diff / minute}m ago."
            diff < day -> "Last session: ${diff / hour}h ago."
            else -> "Last session: ${diff / day}d ago."
        }
    }

    private fun motivationalMessageFor(lastActiveTimestamp: Long): String {
        if (lastActiveTimestamp <= 0L) {
            return "A fresh page is ready for your dragon."
        }

        val diff = (System.currentTimeMillis() - lastActiveTimestamp).coerceAtLeast(0L)
        val hour = 60L * 60L * 1000L
        val day = 24L * hour

        return when {
            diff < day -> "You kept the rhythm alive."
            diff < 3L * day -> "Your dragon is happy you came back."
            diff < 7L * day -> "A small return is still a win."
            else -> "Your dragon has been patiently waiting for you."
        }
    }

    companion object {
        private const val PREFS_NAME = "activity_timeline_engine"
        private const val KEY_LAST_LOGIN_DAY = "last_login_day"
        private const val KEY_LAST_ACTIVE_SESSION_TIMESTAMP = "last_active_session_timestamp"
        private const val KEY_EVOLUTION_MILESTONE_NEARING = "evolution_milestone_nearing"
        private const val EVOLUTION_MILESTONE_NEARING_PROGRESS = 0.8f

        fun log(message: String) {
            Log.d("ActivityTimelineEngine", message)
        }
    }
}
