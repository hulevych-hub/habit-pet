package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleStats = StatisticsEntity(
        id = 1,
        totalCoins = 128,
        currentStreak = 4,
        bestStreak = 12,
        totalCompletions = 47,
        totalXp = 180,
        daysActive = 21,
        totalHabitsCompleted = 8,
        currentCombo = 2,
        bestCombo = 5,
        petAgeDays = 30,
        lastStreakDate = 0L,
        lastStreakAwardedAt = 0,
        globalStreak = 4,
        streakFreezeDatesJson = "[]",
        lastStreakFreezeDate = 0L,
        lastFrozenStreakDate = 0L,
        lastUpdated = 0L,
        lastHabitCompletionTimestamp = 0L
    )

    @Test
    fun statisticsScreen_displaysTopBarTitle() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = sampleStats,
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Journey Insights").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysStreakMilestones() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = sampleStats,
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Lifetime Milestones").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysTotalActions() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = sampleStats,
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Total Actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("47").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysExperienceGained() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = sampleStats,
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Experience Gained").assertIsDisplayed()
        composeTestRule.onNodeWithText("180 XP").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysCoinsEarned() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = sampleStats,
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Coins Earned").assertIsDisplayed()
        composeTestRule.onNodeWithText("128").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_displaysBestCombo() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = sampleStats,
                    isLoading = false
                )
            }
        }

        composeTestRule.onNodeWithText("Best Combo").assertIsDisplayed()
        composeTestRule.onNodeWithText("5x").assertIsDisplayed()
    }

    @Test
    fun statisticsScreen_showsLoadingMessageWhenLoading() {
        composeTestRule.setContent {
            HabitPetTheme {
                StatisticsScreenContent(
                    stats = StatisticsEntity(),
                    isLoading = true
                )
            }
        }

        composeTestRule.onNodeWithText("Counting your journey milestones...").assertIsDisplayed()
    }
}