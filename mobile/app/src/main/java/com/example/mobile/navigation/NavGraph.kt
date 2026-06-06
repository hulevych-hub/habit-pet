package com.example.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobile.presentation.ui.screens.AchievementScreen
import com.example.mobile.presentation.ui.screens.HabitCreationScreen
import com.example.mobile.presentation.ui.screens.HabitDetailScreen
import com.example.mobile.presentation.ui.screens.HabitEditScreen
import com.example.mobile.presentation.ui.screens.HabitsScreen
import com.example.mobile.presentation.ui.screens.HomeScreen
import com.example.mobile.presentation.ui.screens.JournalScreen
import com.example.mobile.presentation.ui.screens.NotificationSettingsScreen
import com.example.mobile.presentation.ui.screens.PetScreen
import com.example.mobile.presentation.ui.screens.RewardsScreen
import com.example.mobile.presentation.ui.screens.StatisticsScreen
import com.example.mobile.ui.theme.HabitPetTheme

@Composable
fun HabitPetNavGraph(navController: NavHostController = rememberNavController()) {
    HabitPetTheme {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                HabitPetBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigateToHabits = { navController.navigate("habits") },
                        onNavigateToHabitDetail = { habitId -> navController.navigate("habitDetail/$habitId") }
                    )
                }
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
                composable("achievements") { AchievementScreen() }
                composable("journal") { JournalScreen() }
                composable("notification_settings") { NotificationSettingsScreen() }
            }
        }
    }
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
private fun HabitPetBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val destinations = listOf(
        BottomDestination("home", "Home", Icons.Default.Home),
        BottomDestination("habits", "Habits", Icons.Default.Checklist),
        BottomDestination("pet", "Pet", Icons.Default.Pets),
        BottomDestination("rewards", "Rewards", Icons.Default.CardGiftcard),
        BottomDestination("statistics", "Stats", Icons.Default.BarChart),
        BottomDestination("achievements", "Achievements", Icons.Default.FavoriteBorder),
        BottomDestination("journal", "Journal", Icons.Default.Book),
        BottomDestination("notification_settings", "Notifications", Icons.Default.Notifications)
    )

    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) }
            )
        }
    }
}
