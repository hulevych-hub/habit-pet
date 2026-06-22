package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.presentation.ui.components.LoadingStateCard
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.DesignTokens
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val statistics = statisticsRepository.getStatistics()

    init {
        viewModelScope.launch {
            statistics.collectLatest { _isLoading.value = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(statisticsViewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by statisticsViewModel.statistics.collectAsState(initial = StatisticsEntity())
    val isLoading by statisticsViewModel.isLoading.collectAsState()

    StatisticsScreenContent(stats = stats, isLoading = isLoading)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    stats: StatisticsEntity,
    isLoading: Boolean
) {
    Scaffold(
        containerColor = AppTheme.current.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Journey Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.ink
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppTheme.current.headerSurface),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DesignTokens.Section.horizontalPadding),
                message = "Counting your journey milestones..."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = DesignTokens.Section.horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space14),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.space14),
            contentPadding = PaddingValues(top = DesignTokens.Section.horizontalPadding, bottom = DesignTokens.space40)
        ) {
            // Section 1: Hero Streak Dashboard Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroStreakCard(
                    currentStreak = stats.currentStreak,
                    bestStreak = stats.bestStreak
                )
            }

            // Section Divider Label
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "Lifetime Milestones",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = AppTheme.current.muted,
                    modifier = Modifier.padding(top = DesignTokens.space8, bottom = DesignTokens.space2)
                )
            }

            // Section 2: Core Metrics Grid Items
            item {
                StatBentoCard(
                    label = "Total Actions",
                    value = "${stats.totalCompletions}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = AppTheme.current.success
                )
            }
            item {
                StatBentoCard(
                    label = "Experience Gained",
                    value = "${stats.totalXp} XP",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = AppTheme.current.violet
                )
            }
            item {
                StatBentoCard(
                    label = "Days Active",
                    value = "${stats.daysActive} d",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = AppTheme.current.blue
                )
            }
            item {
                StatBentoCard(
                    label = "Habits Formed",
                    value = "${stats.totalHabitsCompleted}",
                    icon = Icons.Default.TaskAlt,
                    accentColor = AppTheme.current.purple
                )
            }

            // Section 3: Pet Lifecycle Statistics Wide Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                PetLifecycleCard(ageDays = stats.petAgeDays)
            }
        }
    }
    }
}

@Composable
private fun HeroStreakCard(
    currentStreak: Int,
    bestStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.radius6xl),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AppTheme.current.primary, AppTheme.current.primaryContainer)
                    )
                )
                .padding(DesignTokens.space24)
        ) {
            // Ambient subtle background glow styling
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = DesignTokens.Section.horizontalPadding, y = -DesignTokens.space10)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(AppTheme.current.danger.copy(alpha = DesignTokens.alpha16), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.space4)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space6)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = AppTheme.current.danger,
                            modifier = Modifier.size(DesignTokens.Icon.sizeMd)
                        )
                        Text(
                            text = "CURRENT STREAK",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = AppTheme.current.danger
                        )
                    }

                    Text(
                        text = "$currentStreak Days",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = AppTheme.current.onPrimary
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.space12))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.space6),
                        modifier = Modifier
                            .background(AppTheme.current.onPrimary.copy(alpha = DesignTokens.alpha12), DesignTokens.cardCornerCircle)
                            .padding(horizontal = DesignTokens.space12, vertical = DesignTokens.space6)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = AppTheme.current.gold,
                            modifier = Modifier.size(DesignTokens.Icon.sizeXs)
                        )
                        Text(
                            text = "Personal Best: $bestStreak days",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AppTheme.current.onPrimary.copy(alpha = DesignTokens.alpha85)
                        )
                    }
                }

                // Oversized high impact numerical fire badge
                Box(
                    modifier = Modifier
                        .size(DesignTokens.space72)
                        .background(AppTheme.current.danger.copy(alpha = DesignTokens.alpha12), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AppTheme.current.danger,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBentoCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DesignTokens.radius4xl),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.elevationSm)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.space18),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.space12)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.Card.iconSize)
                    .background(accentColor.copy(alpha = DesignTokens.alpha12), RoundedCornerShape(DesignTokens.Card.iconBackgroundRadius)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(DesignTokens.Card.iconInnerSize)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.space2)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.current.ink
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = AppTheme.current.muted
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun StatisticsScreenPreview() {
    HabitPetTheme {
        StatisticsScreenContent(
            stats = StatisticsEntity(
                currentStreak = 4,
                bestStreak = 7,
                totalCompletions = 28,
                totalXp = 320,
                daysActive = 6,
                totalHabitsCompleted = 24,
                petAgeDays = 6
            ),
            isLoading = false
        )
    }
}

@Composable
private fun PetLifecycleCard(ageDays: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DesignTokens.space6),
        shape = RoundedCornerShape(DesignTokens.radius4xl),
        colors = CardDefaults.cardColors(containerColor = AppTheme.current.card),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.elevationSm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Section.horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.space16)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(AppTheme.current.gold.copy(alpha = DesignTokens.alpha12), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = null,
                        tint = AppTheme.current.gold,
                        modifier = Modifier.size(DesignTokens.Card.iconInnerSize)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(DesignTokens.space2)) {
                    Text(
                        text = "Companion Bond Age",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.ink
                    )
                    Text(
                        text = "Total days spent nurturing your dragon companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.current.muted
                    )
                }
            }

            Text(
                text = "$ageDays d",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = AppTheme.current.gold
            )
        }
    }
}
