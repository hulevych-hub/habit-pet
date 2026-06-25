package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.GameEventRarity
import com.example.mobile.domain.GameEventType
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ActivityTimelineScreenUiTest {

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

    private val sampleEvents = listOf(
        GameEventEntity(
            id = 1,
            type = GameEventType.HABIT_COMPLETED.name,
            timestamp = System.currentTimeMillis() - 60 * 60_000L,
            title = "Morning hydration completed",
            description = "Your dragon celebrated a small, steady win.",
            rarity = GameEventRarity.RARE.name
        ),
        GameEventEntity(
            id = 2,
            type = GameEventType.LEVEL_UP.name,
            timestamp = System.currentTimeMillis() - 3 * 60 * 60_000L,
            title = "Level 3 reached",
            description = "A new tier of care unlocked for your dragon.",
            rarity = GameEventRarity.EPIC.name
        )
    )

    @Test
    fun activityTimelineScreen_displaysEvents() {
        composeTestRule.setContent {
            HabitPetTheme {
                ActivityTimelineScreenContent(
                    progressUiState = sampleUiState,
                    events = sampleEvents,
                    isLoading = false,
                    isLoadingMore = false,
                    hasMore = true,
                    onNavigateToRewardsLocked = {},
                    onLoadMore = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Morning hydration completed").assertIsDisplayed()
    }

    @Test
    fun activityTimelineScreen_displaysLoadMoreButton() {
        composeTestRule.setContent {
            HabitPetTheme {
                ActivityTimelineScreenContent(
                    progressUiState = sampleUiState,
                    events = sampleEvents,
                    isLoading = false,
                    isLoadingMore = false,
                    hasMore = true,
                    onNavigateToRewardsLocked = {},
                    onLoadMore = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Load Older Chronicles").assertIsDisplayed()
    }

    @Test
    fun activityTimelineScreen_showsLoadingStateWhenLoading() {
        composeTestRule.setContent {
            HabitPetTheme {
                ActivityTimelineScreenContent(
                    progressUiState = sampleUiState,
                    events = emptyList(),
                    isLoading = true,
                    isLoadingMore = false,
                    hasMore = false,
                    onNavigateToRewardsLocked = {},
                    onLoadMore = {},
                    onStreakClick = {},
                    onStreakCalendarDismiss = {},
                    onPreviousStreakMonth = {},
                    onNextStreakMonth = {},
                    streakCalendarState = null
                )
            }
        }

        composeTestRule.onNodeWithText("Opening your journey chronicle...").assertIsDisplayed()
    }
}
