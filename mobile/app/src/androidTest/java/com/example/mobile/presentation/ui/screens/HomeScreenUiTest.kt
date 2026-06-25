package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePet = PetEntity(
        id = 1L,
        name = "Luna",
        level = 3,
        xp = 180L,
        evolutionStage = 1,
        mood = "Calm"
    )

    private val sampleHabits = listOf(
        HabitEntity(
            id = 1L,
            name = "Morning Hydration",
            icon = "",
            type = "CHECKBOX",
            minimumDurationMinutes = 0,
            currentStreak = 5,
            bestStreak = 10
        ),
        HabitEntity(
            id = 2L,
            name = "Evening Walk",
            icon = "",
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
    fun homeScreen_displaysPetSummary() {
        composeTestRule.setContent {
            HabitPetTheme {
                HomeScreenContent(
                    uiState = sampleUiState,
                    isLoading = false,
                    onNavigateToHabits = {},
                    onNavigateToHabitDetail = {},
                    onNavigateToRewardsLocked = {},
                    onRenamePet = {},
                    onResetGameData = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null,
                    streakFreezePrompt = null,
                    onUseStreakFreeze = {},
                    onDismissStreakFreeze = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Luna").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysHeaderWithStreakAndCoins() {
        composeTestRule.setContent {
            HabitPetTheme {
                HomeScreenContent(
                    uiState = sampleUiState,
                    isLoading = false,
                    onNavigateToHabits = {},
                    onNavigateToHabitDetail = {},
                    onNavigateToRewardsLocked = {},
                    onRenamePet = {},
                    onResetGameData = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null,
                    streakFreezePrompt = null,
                    onUseStreakFreeze = {},
                    onDismissStreakFreeze = {}
                )
            }
        }

        composeTestRule.onNodeWithText("128").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysHabitList() {
        composeTestRule.setContent {
            HabitPetTheme {
                HomeScreenContent(
                    uiState = sampleUiState,
                    isLoading = false,
                    onNavigateToHabits = {},
                    onNavigateToHabitDetail = {},
                    onNavigateToRewardsLocked = {},
                    onRenamePet = {},
                    onResetGameData = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null,
                    streakFreezePrompt = null,
                    onUseStreakFreeze = {},
                    onDismissStreakFreeze = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Morning Hydration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Evening Walk").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsLoadingStateWhenLoading() {
        composeTestRule.setContent {
            HabitPetTheme {
                HomeScreenContent(
                    uiState = sampleUiState.copy(pet = samplePet.copy(id = 0L, name = "")),
                    isLoading = true,
                    onNavigateToHabits = {},
                    onNavigateToHabitDetail = {},
                    onNavigateToRewardsLocked = {},
                    onRenamePet = {},
                    onResetGameData = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null,
                    streakFreezePrompt = null,
                    onUseStreakFreeze = {},
                    onDismissStreakFreeze = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Waking up your dragon...").assertIsDisplayed()
    }
}
