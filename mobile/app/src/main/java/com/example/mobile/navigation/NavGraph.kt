package com.example.mobile.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobile.presentation.ui.animations.HabitPetAnimations
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.screens.AchievementScreen
import com.example.mobile.presentation.ui.screens.ActivityTimelineScreen
import com.example.mobile.presentation.ui.screens.HabitCreationScreen
import com.example.mobile.presentation.ui.screens.HabitDetailScreen
import com.example.mobile.presentation.ui.screens.HabitEditScreen
import com.example.mobile.presentation.ui.screens.HabitsScreen
import com.example.mobile.presentation.ui.screens.HomeScreen
import com.example.mobile.presentation.ui.screens.HomeScreenViewModel
import com.example.mobile.presentation.ui.screens.NotificationSettingsScreen
import com.example.mobile.presentation.ui.screens.OnboardingScreen
import com.example.mobile.presentation.ui.screens.PetScreen
import com.example.mobile.presentation.ui.screens.RewardsScreen
import com.example.mobile.presentation.ui.screens.StatisticsScreen
import com.example.mobile.presentation.viewmodel.AchievementViewModel
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.AppThemeOption
import com.example.mobile.ui.theme.AppThemePrefs
import com.example.mobile.ui.theme.HabitPetTheme
import com.example.mobile.util.OnboardingPrefs

private object AppRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val HABITS = "habits"
    const val PET = "pet"
    const val REWARDS = "rewards"
    const val REWARDS_LOCKED = "locked"
    const val REWARDS_OWNED = "owned"
    const val ACHIEVEMENTS = "achievements"
    const val SETTINGS = "notification_settings"
    const val HABIT_CREATION = "habitCreation"
    const val HABIT_DETAIL = "habitDetail"
    const val HABIT_EDIT = "habitEdit"
    const val STATISTICS = "statistics"
    const val ACTIVITY = "activity"

    fun habitDetail(habitId: Long) = "$HABIT_DETAIL/$habitId"
    fun habitEdit(habitId: Long) = "$HABIT_EDIT/$habitId"
    fun rewards(collection: String) = "rewards/$collection"
}

private sealed class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
) {
    data object Home : BottomDestination(AppRoutes.HOME, "Home", Icons.Default.Home)
    data object Habits : BottomDestination(AppRoutes.HABITS, "Habits", Icons.Default.Checklist)
    data object Pet : BottomDestination(AppRoutes.PET, "Pet", Icons.Default.Pets)
    data object Achievements : BottomDestination(
        route = AppRoutes.ACHIEVEMENTS,
        label = "Achievements",
        icon = Icons.Default.FavoriteBorder,
        badgeCount = 0
    )
    data object Settings : BottomDestination(AppRoutes.SETTINGS, "Settings", Icons.Default.Settings)

    companion object {
        fun all(claimableAchievementCount: Int): List<BottomDestination> = listOf(
            Home,
            Habits,
            Pet,
            if (claimableAchievementCount > 0) {
                AchievementsWithBadge(claimableAchievementCount)
            } else {
                Achievements
            },
            Settings
        )
    }
}

private data class AchievementsWithBadge(
    val count: Int
) : BottomDestination(
    route = AppRoutes.ACHIEVEMENTS,
    label = "Achievements",
    icon = Icons.Default.FavoriteBorder,
    badgeCount = count
)

@Composable
fun HabitPetNavGraph(
    navController: NavHostController = rememberNavController(),
    appTheme: AppThemeOption = AppThemePrefs.currentTheme(),
    microFeedbackManager: MicroFeedbackManager? = null
) {
    val context = LocalContext.current
    val startDestination = if (OnboardingPrefs.hasSeenOnboarding(context)) {
        AppRoutes.HOME
    } else {
        AppRoutes.ONBOARDING
    }

    HabitPetTheme(appTheme = appTheme) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val achievementViewModel: AchievementViewModel = hiltViewModel()
        val homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
        val claimableAchievementCount by achievementViewModel.claimableAchievementCount.collectAsState()
        val bottomDestinations = BottomDestination.all(claimableAchievementCount)
        val isRewardsRoute = currentRoute == AppRoutes.REWARDS ||
            currentRoute?.startsWith("${AppRoutes.REWARDS}/") == true
        val selectedBottomRoute = when {
            currentRoute?.startsWith("${AppRoutes.REWARDS}/${AppRoutes.REWARDS_OWNED}") == true -> BottomDestination.Pet.route
            else -> currentRoute
        }
        val shouldShowBottomBar = bottomDestinations.any { currentRoute == it.route } || isRewardsRoute

        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    HabitPetBottomBar(
                        destinations = bottomDestinations,
                        currentRoute = selectedBottomRoute,
                        onNavigate = { destination ->
                            navigateToBottomDestination(
                                navController = navController,
                                destination = destination,
                                currentRoute = currentRoute,
                                microFeedbackManager = microFeedbackManager
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    // Fixed: Apply ONLY the bottom padding generated by your custom NavigationBar component shell,
                    // letting top bars natively flush straight up with no structural margins.
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(
                            HabitPetAnimations.Duration.NORMAL,
                            easing = HabitPetAnimations.Easing.STANDARD
                        )
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(
                            HabitPetAnimations.Duration.NORMAL,
                            easing = HabitPetAnimations.Easing.STANDARD
                        )
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(
                            HabitPetAnimations.Duration.NORMAL,
                            easing = HabitPetAnimations.Easing.STANDARD
                        )
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(
                            HabitPetAnimations.Duration.NORMAL,
                            easing = HabitPetAnimations.Easing.STANDARD
                        )
                    )
                }
            ) {
                composable(AppRoutes.ONBOARDING) {
                    OnboardingScreen(
                        onComplete = {
                            OnboardingPrefs.markOnboardingSeen(context)
                            navController.navigate(AppRoutes.HABIT_CREATION) {
                                popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                            }
                        },
                        onSkip = {
                            OnboardingPrefs.markOnboardingSeen(context)
                            navController.navigate(AppRoutes.HOME) {
                                popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                            }
                        }
                    )
                }
                composable(AppRoutes.HOME) {
                    HomeScreen(
                        onNavigateToHabits = { navigateToBottomDestination(navController, BottomDestination.Habits, microFeedbackManager) },
                        onNavigateToHabitDetail = { habitId -> navController.navigate(AppRoutes.habitDetail(habitId)) },
                        onNavigateToRewardsLocked = { navigateToRewards(navController, AppRoutes.REWARDS_LOCKED, microFeedbackManager) },
                        homeScreenViewModel = homeScreenViewModel
                    )
                }
                composable(AppRoutes.HABITS) {
                    HabitsScreen(
                        navController = navController,
                        homeScreenViewModel = homeScreenViewModel,
                        onNavigateToRewardsLocked = { navigateToRewards(navController, AppRoutes.REWARDS_LOCKED, microFeedbackManager) }
                    )
                }
                composable(AppRoutes.HABIT_CREATION) {
                    HabitCreationScreen(
                        onNavigateUp = { navController.popBackStack() },
                        onHabitCreated = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "${AppRoutes.HABIT_EDIT}/{habitId}",
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
                    route = "${AppRoutes.HABIT_DETAIL}/{habitId}",
                    arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                ) {
                    val habitId = it.arguments?.getLong("habitId") ?: 0L
                    HabitDetailScreen(
                        habitId = habitId,
                        onNavigateUp = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.PET) {
                    PetScreen(
                        homeScreenViewModel = homeScreenViewModel,
                        onNavigateToRewardsLocked = { navigateToRewards(navController, AppRoutes.REWARDS_LOCKED, microFeedbackManager) },
                        onNavigateToRewardsOwned = { navigateToRewards(navController, AppRoutes.REWARDS_OWNED, microFeedbackManager) }
                    )
                }
                composable(
                    route = "${AppRoutes.REWARDS}/{collection}",
                    arguments = listOf(navArgument("collection") { type = NavType.StringType })
                ) { entry ->
                    val collection = entry.arguments?.getString("collection") ?: AppRoutes.REWARDS_OWNED
                    RewardsScreen(
                        homeScreenViewModel = homeScreenViewModel,
                        initialCollection = collection,
                        onNavigateToRewardsLocked = { navigateToRewards(navController, AppRoutes.REWARDS_LOCKED, microFeedbackManager) }
                    )
                }
                composable(AppRoutes.STATISTICS) { StatisticsScreen() }
                composable(AppRoutes.ACHIEVEMENTS) {
                    AchievementScreen(
                        homeScreenViewModel = homeScreenViewModel
                    )
                }
                composable(AppRoutes.ACTIVITY) {
                    ActivityTimelineScreen(
                        homeScreenViewModel = homeScreenViewModel,
                        onNavigateToRewardsLocked = { navigateToRewards(navController, AppRoutes.REWARDS_LOCKED, microFeedbackManager) }
                    )
                }
                composable(AppRoutes.SETTINGS) {
                    NotificationSettingsScreen(
                        homeScreenViewModel = homeScreenViewModel,
                        onNavigateToRewardsLocked = { navigateToRewards(navController, AppRoutes.REWARDS_LOCKED, microFeedbackManager) }
                    )
                }
            }
        }
    }
}

private fun navigateToBottomDestination(
    navController: NavHostController,
    destination: BottomDestination,
    microFeedbackManager: MicroFeedbackManager?,
    currentRoute: String? = null
) {
    microFeedbackManager?.triggerTabSwitched()
    if (currentRoute?.startsWith("${AppRoutes.REWARDS}/") == true) {
        navController.navigate(destination.route) {
            popUpTo(currentRoute) {
                inclusive = true
            }
            launchSingleTop = true
        }
        return
    }

    navController.navigate(destination.route) {
        popUpTo(navController.graph.startDestinationId) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateToRewards(
    navController: NavHostController,
    collection: String,
    microFeedbackManager: MicroFeedbackManager?
) {
    microFeedbackManager?.triggerTabSwitched()
    navController.navigate(AppRoutes.rewards(collection)) {
        launchSingleTop = true
    }
}


@Composable
private fun HabitPetBottomBar(
    destinations: List<BottomDestination>,
    currentRoute: String?,
    onNavigate: (BottomDestination) -> Unit
) {
    NavigationBar(
        containerColor = AppTheme.current.headerSurface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            destinations.forEach { destination ->
                HabitPetBottomNavItem(
                    destination = destination,
                    selected = currentRoute == destination.route,
                    modifier = Modifier.weight(1f),
                    onNavigate = { onNavigate(destination) }
                )
            }
        }
    }
}

@Composable
private fun HabitPetBottomNavItem(
    destination: BottomDestination,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onNavigate: () -> Unit
) {
    val indicatorWidth by animateDpAsState(if (selected) 58.dp else 48.dp)

    Box(
        modifier = modifier
            .height(64.dp)
            .clickable(onClick = onNavigate),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(40.dp)
                .background(
                    if (selected) AppTheme.current.primaryContainer.copy(alpha = 0.55f) else Color.Transparent,
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                BottomBarIcon(destination, selected)
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) AppTheme.current.violet else AppTheme.current.muted
                )
            }
        }
    }
}

@Composable
private fun BottomBarIcon(destination: BottomDestination, selected: Boolean) {
    val isAchievementBadgeVisible = destination.route == AppRoutes.ACHIEVEMENTS && destination.badgeCount > 0
    val contentDescription = if (isAchievementBadgeVisible) {
        "${destination.label}, ${destination.badgeCount} achievements ready to claim"
    } else {
        destination.label
    }

    if (isAchievementBadgeVisible) {
        Box(contentAlignment = Alignment.CenterEnd) {
            Icon(
                imageVector = destination.icon,
                contentDescription = contentDescription,
                tint = if (selected) AppTheme.current.violet else AppTheme.current.muted,
                modifier = Modifier.padding(end = 8.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp) // Adjusted slightly smaller for visual elegance
                    .background(AppTheme.current.success, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = destination.badgeCount.toString(),
                    color = AppTheme.current.onPrimary,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    } else {
        Icon(
            imageVector = destination.icon,
            contentDescription = contentDescription,
            tint = if (selected) AppTheme.current.violet else AppTheme.current.muted
        )
    }
}