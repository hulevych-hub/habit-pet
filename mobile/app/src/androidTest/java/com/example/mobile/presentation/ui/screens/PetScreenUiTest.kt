package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PetScreenUiTest {

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

    private val sampleUiState = HomeScreenViewModel.UiState(
        globalStreak = 4,
        habits = emptyList(),
        pet = samplePet,
        completedTodayXp = emptyMap(),
        totalCoins = 128,
        lastStreakDate = 0L,
        currentCombo = 0,
        lastHabitCompletionTimestamp = 0L,
        globalStreakCompletedToday = false
    )

    @Test
    fun petScreen_displaysPetName() {
        composeTestRule.setContent {
            HabitPetTheme {
                PetScreenContent(
                    uiState = sampleUiState,
                    isLoading = false,
                    error = null,
                    progressFraction = 0.48f,
                    currentLevelXp = 80L,
                    xpRequiredForNextLevel = 165L,
                    onNavigateToRewardsLocked = {},
                    onNavigateToRewardsOwned = {},
                    onClearError = {},
                    onConfirmRename = { _, _ -> },
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Luna").assertIsDisplayed()
    }

    @Test
    fun petScreen_displaysLevel() {
        composeTestRule.setContent {
            HabitPetTheme {
                PetScreenContent(
                    uiState = sampleUiState,
                    isLoading = false,
                    error = null,
                    progressFraction = 0.48f,
                    currentLevelXp = 80L,
                    xpRequiredForNextLevel = 165L,
                    onNavigateToRewardsLocked = {},
                    onNavigateToRewardsOwned = {},
                    onClearError = {},
                    onConfirmRename = { _, _ -> },
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Lv. 3").assertIsDisplayed()
    }

    @Test
    fun petScreen_showsLoadingStateWhenLoading() {
        composeTestRule.setContent {
            HabitPetTheme {
                PetScreenContent(
                    uiState = sampleUiState,
                    isLoading = true,
                    error = null,
                    progressFraction = 0.48f,
                    currentLevelXp = 80L,
                    xpRequiredForNextLevel = 165L,
                    onNavigateToRewardsLocked = {},
                    onNavigateToRewardsOwned = {},
                    onClearError = {},
                    onConfirmRename = { _, _ -> },
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Checking on your dragon...").assertIsDisplayed()
    }
}
