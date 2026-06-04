package com.example.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobile.presentation.ui.screens.HabitCreationScreen
import com.example.mobile.presentation.ui.screens.HabitDetailScreen
import com.example.mobile.presentation.ui.screens.HabitEditScreen
import com.example.mobile.presentation.ui.screens.HabitsScreen
import com.example.mobile.presentation.ui.screens.HomeScreen
import com.example.mobile.presentation.ui.screens.PetScreen
import com.example.mobile.presentation.ui.screens.RewardsScreen
import com.example.mobile.presentation.ui.screens.StatisticsScreen
import com.example.mobile.ui.theme.HabitPetTheme

@Composable
fun HabitPetNavGraph(navController: NavHostController = rememberNavController()) {
    HabitPetTheme {
        Scaffold(
            // We'll add a bottom navigation bar later
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("home") { HomeScreen() }
                composable("habits") { HabitsScreen(navController = navController) }
                composable("habitCreation") {
                    HabitCreationScreen(
                        onNavigateUp = { navController.popBackStack() },
                        onHabitCreated = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "habitEdit/{habitId}",
                    arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                ) {
                    val habitId = it.arguments?.getLong("habitId") ?: 0L
                    HabitEditScreen(
                        habitId = habitId,
                        onNavigateUp = { navController.popBackStack() },
                        onHabitUpdated = { navController.popBackStack() },
                        onHabitDeleted = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "habitDetail/{habitId}",
                    arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                ) {
                    val habitId = it.arguments?.getLong("habitId") ?: 0L
                    HabitDetailScreen(
                        habitId = habitId,
                        onNavigateUp = { navController.popBackStack() }
                    )
                }
                composable("pet") { PetScreen() }
                composable("rewards") { RewardsScreen() }
                composable("statistics") { StatisticsScreen() }
            }
        }
    }
}
