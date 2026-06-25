package com.example.mobile.presentation.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mobile.navigation.HabitPetNavGraph
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navGraph_rendersWithoutCrash() {
        // The HabitPetNavGraph composable should render without crashing.
        // It uses NavHost with multiple routes and ViewModels.
        // This test validates the navigation graph builds correctly.
        composeTestRule.setContent {
            HabitPetTheme {
                HabitPetNavGraph()
            }
        }

        // The graph renders the HomeScreen by default.
        // We verify the tree builds without error — if there's a routing or DI issue,
        // the composable would fail to render.
    }

    @Test
    fun navGraph_bottomBarIsRendered() {
        // The bottom navigation bar should be visible on the home screen.
        composeTestRule.setContent {
            HabitPetTheme {
                HabitPetNavGraph()
            }
        }

        // The bottom bar contains "Home" text — verify it's displayed.
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
