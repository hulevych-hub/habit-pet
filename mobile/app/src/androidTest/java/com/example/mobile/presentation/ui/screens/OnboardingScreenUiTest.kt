package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OnboardingScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboardingScreen_displaysWelcomeText() {
        composeTestRule.setContent {
            HabitPetTheme {
                OnboardingScreen(
                    onComplete = {},
                    onSkip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome to").assertIsDisplayed()
        composeTestRule.onNodeWithText("Habit Pet").assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_displaysAllFourSteps() {
        composeTestRule.setContent {
            HabitPetTheme {
                OnboardingScreen(
                    onComplete = {},
                    onSkip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Create a tiny quest").assertIsDisplayed()
        composeTestRule.onNodeWithText("Complete it to earn XP & coins").assertIsDisplayed()
        composeTestRule.onNodeWithText("Watch your dragon grow").assertIsDisplayed()
        composeTestRule.onNodeWithText("Earn rewards & celebrate").assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_displaysSkipButton() {
        composeTestRule.setContent {
            HabitPetTheme {
                OnboardingScreen(
                    onComplete = {},
                    onSkip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Skip").assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_displaysCtaButton() {
        composeTestRule.setContent {
            HabitPetTheme {
                OnboardingScreen(
                    onComplete = {},
                    onSkip = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Create your first habit").assertIsDisplayed()
    }

    @Test
    fun onboardingScreen_displaysSubtitle() {
        composeTestRule.setContent {
            HabitPetTheme {
                OnboardingScreen(
                    onComplete = {},
                    onSkip = {}
                )
            }
        }

        composeTestRule.onNodeWithText(
            "Turn tiny habits into a thriving dragon companion."
        ).assertIsDisplayed()
    }
}
