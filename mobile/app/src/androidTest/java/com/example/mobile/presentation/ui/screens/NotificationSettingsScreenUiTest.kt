package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.presentation.ui.screens.HomeScreenViewModel
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NotificationSettingsScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val samplePet = PetEntity(
        id = 1,
        name = "Luna",
        level = 3,
        xp = 180,
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
    fun notificationSettingsScreen_displaysHeroTitle() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Gentle Nudges").assertIsDisplayed()
    }

    @Test
    fun notificationSettingsScreen_displaysHeroSubtitle() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Keep reminders warm and supportive. Your companion invites you back without pressure."
        ).assertIsDisplayed()
    }

    @Test
    fun notificationSettingsScreen_displaysThemeSection() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("App Color Palette").assertIsDisplayed()
    }

    @Test
    fun notificationSettingsScreen_displaysDailyReminderRow() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Dragon Waiting").assertIsDisplayed()
    }

    @Test
    fun notificationSettingsScreen_displaysStreakReminderRow() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Streak Encouragement").assertIsDisplayed()
    }

    @Test
    fun notificationSettingsScreen_displaysPetBondReminderRow() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Pet Bond Reminder").assertIsDisplayed()
    }

    @Test
    fun notificationSettingsScreen_displaysCopyrightFooter() {
        composeTestRule.setContent {
            HabitPetTheme {
                NotificationSettingsContent(
                    progressUiState = sampleUiState,
                    onNavigateToRewardsLocked = {},
                    onError = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Copyright © 2026 Hulevych Enterprises. All rights to cuddle Vanessa Baron reserved."
        ).assertIsDisplayed()
    }
}
