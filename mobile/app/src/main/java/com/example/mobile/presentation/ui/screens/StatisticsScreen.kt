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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {
    val statistics = statisticsRepository.getStatistics()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(statisticsViewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by statisticsViewModel.statistics.collectAsState(initial = StatisticsEntity())

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Journey Insights",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteStats.Ink
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
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
                    color = ColorPaletteStats.Muted,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            // Section 2: Core Metrics Grid Items
            item {
                StatBentoCard(
                    label = "Total Actions",
                    value = "${stats.totalCompletions}",
                    icon = Icons.Default.CheckCircle,
                    accentColor = ColorPaletteStats.Mint
                )
            }
            item {
                StatBentoCard(
                    label = "Experience Gained",
                    value = "${stats.totalXp} XP",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = ColorPaletteStats.Violet
                )
            }
            item {
                StatBentoCard(
                    label = "Days Active",
                    value = "${stats.daysActive} d",
                    icon = Icons.Default.CalendarMonth,
                    accentColor = ColorPaletteStats.Ocean
                )
            }
            item {
                StatBentoCard(
                    label = "Habits Formed",
                    value = "${stats.totalHabitsCompleted}",
                    icon = Icons.Default.TaskAlt,
                    accentColor = ColorPaletteStats.Lavender
                )
            }

            // Section 3: Pet Lifecycle Statistics Wide Card
            item(span = { GridItemSpan(maxLineSpan) }) {
                PetLifecycleCard(ageDays = stats.petAgeDays)
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteStats.Ink)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ColorPaletteStats.Ink, Color(0xFF272144))
                    )
                )
                .padding(24.dp)
        ) {
            // Ambient subtle background glow styling
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp, y = (-10).dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(ColorPaletteStats.Flame.copy(alpha = 0.18f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = ColorPaletteStats.Flame,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "CURRENT STREAK",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ColorPaletteStats.Flame
                        )
                    }

                    Text(
                        text = "$currentStreak Days",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.07f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = ColorPaletteStats.Gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Personal Best: $bestStreak days",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Oversized high impact numerical fire badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(ColorPaletteStats.Flame.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = ColorPaletteStats.Flame,
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteStats.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteStats.Ink
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = ColorPaletteStats.Muted
                )
            }
        }
    }
}

@Composable
private fun PetLifecycleCard(ageDays: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteStats.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(ColorPaletteStats.Gold.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = null,
                        tint = ColorPaletteStats.Gold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Companion Bond Age",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteStats.Ink
                    )
                    Text(
                        text = "Total days spent nurturing your dragon companion",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorPaletteStats.Muted
                    )
                }
            }

            Text(
                text = "$ageDays d",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = ColorPaletteStats.Gold
            )
        }
    }
}

private object ColorPaletteStats {
    val Card = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1E1A34)
    val Muted = Color(0xFF7E7A94)

    // Theme Accent Markers
    val Flame = Color(0xFFFF6B35)
    val Gold = Color(0xFFFFB84D)
    val Mint = Color(0xFF23A160)
    val Violet = Color(0xFF8A76F9)
    val Ocean = Color(0xFF3B91FF)
    val Lavender = Color(0xFFB26CFF)
}