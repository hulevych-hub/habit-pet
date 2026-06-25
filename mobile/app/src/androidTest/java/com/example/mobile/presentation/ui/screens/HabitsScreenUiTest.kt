package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.navigation.compose.rememberNavController
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.repository.ChallengeUiState
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HabitsScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePet = PetEntity(
        id = 1L,
        name = "Luna",
        level = 3,
        xp = 180L,
        evolutionStage = 1,
        equippedOutfit = "classic_blue_outfit",
        equippedBackground = "misty_meadow_background",
        equippedAura = null,
        mood = "Calm"
    )

    private val sampleHabits = listOf(
        HabitEntity(
            id = 1L,
            name = "Morning Hydration",
            icon = "water",
            type = "CHECKBOX",
            minimumDurationMinutes = 0,
            currentStreak = 5,
            bestStreak = 10
        ),
        HabitEntity(
            id = 2L,
            name = "Evening Walk",
            icon = "walk",
            type = "TIMER",
            minimumDurationMinutes = 30,
            currentStreak = 3,
            bestStreak = 7
        )
    )

    private val sampleUiState = HomeScreenViewModel.UiState(
        globalStreak = 4,
        habits = sampleHabits,
        pet = samplePet,
        completedTodayXp = mapOf(1L to 10L, 2L to 0L),
        totalCoins = 128,
        lastStreakDate = 0L,
        currentCombo = 2,
        lastHabitCompletionTimestamp = System.currentTimeMillis(),
        globalStreakCompletedToday = false
    )

    @Test
    fun habitsScreen_displaysHabitList() {
        composeTestRule.setContent {
            HabitPetTheme {
                HabitsScreenContent(
                    navController = rememberNavController(),
                    habits = sampleHabits,
                    completedToday = mapOf(1L to true, 2L to false),
                    completingHabitIds = emptySet(),
                    error = null,
                    isLoading = false,
                    progressUiState = sampleUiState,
                    challengeUiState = ChallengeUiState.empty(),
                    onRetry = {},
                    onNavigateToRewardsLocked = {},
                    onComplete = {},
                    onDelete = {},
                    onClaimChallenge = {},
                    onHabitStreakClick = {},
                    onGlobalStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Morning Hydration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Evening Walk").assertIsDisplayed()
    }

    @Test
    fun habitsScreen_showsLoadingStateWhenLoading() {
        composeTestRule.setContent {
            HabitPetTheme {
                HabitsScreenContent(
                    navController = rememberNavController(),
                    habits = sampleHabits,
                    completedToday = emptyMap(),
                    completingHabitIds = emptySet(),
                    error = null,
                    isLoading = true,
                    progressUiState = sampleUiState,
                    challengeUiState = ChallengeUiState.empty(),
                    onRetry = {},
                    onNavigateToRewardsLocked = {},
                    onComplete = {},
                    onDelete = {},
                    onClaimChallenge = {},
                    onHabitStreakClick = {},
                    onGlobalStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Gathering today's quests...").assertIsDisplayed()
    }
}
