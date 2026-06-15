package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.GameEventRarity
import com.example.mobile.domain.GameEventType
import com.example.mobile.presentation.ui.components.CoinIcon
import com.example.mobile.presentation.ui.components.LoadingStateCard
import com.example.mobile.presentation.viewmodel.ActivityTimelineViewModel
import com.example.mobile.util.ReinforcementMessageProvider
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTimelineScreen(
    activityTimelineViewModel: ActivityTimelineViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val events by activityTimelineViewModel.events.collectAsState()
    val progressUiState by homeScreenViewModel.uiState.collectAsState()
    val isLoading by activityTimelineViewModel.isLoading.collectAsState()
    val isLoadingMore by activityTimelineViewModel.isLoadingMore.collectAsState()
    val hasMore by activityTimelineViewModel.hasMore.collectAsState()
    val groups = remember(events) { groupEventsByDay(events) }
    val listState = rememberLazyListState()
    var hasAutoScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(events) {
        if (!hasAutoScrolled && events.isNotEmpty()) {
            listState.scrollToItem(0)
            hasAutoScrolled = true
        }
    }

    Scaffold(
        containerColor = Color(0xFFFAFAFC),
        topBar = {
            GamifiedFixedHeader(
                streak = progressUiState.globalStreak,
                coins = progressUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(progressUiState.pet.evolutionStage),
                streakCompletedToday = progressUiState.globalStreakCompletedToday
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingStateCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                message = "Opening your journey chronicle..."
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
            groups.forEach { group ->
                item {
                    DayHeader(label = group.label)
                }

                items(
                    items = group.events,
                    key = { it.id }
                ) { event ->
                    ActivityTimelineItem(event = event)
                }
            }

            if (hasMore) {
                item {
                    Button(
                        onClick = { activityTimelineViewModel.loadMore() },
                        enabled = !isLoadingMore,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPaletteActivity.Violet,
                            disabledContainerColor = ColorPaletteActivity.Violet.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(
                                text = if (isLoadingMore) "Retrieving Chronicles..." else "Load Older Chronicles",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String,
    streakCompletedToday: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        val streakTint = if (streakCompletedToday) ColorPaletteActivity.Flame else Color(0xFFA9A3B8)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = streakTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$streak d",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteActivity.Ink
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ColorPaletteActivity.Violet.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = ColorPaletteActivity.Violet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteActivity.Violet
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CoinIcon(
                    modifier = Modifier.size(22.dp),
                    tint = ColorPaletteActivity.Amber
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteActivity.Ink
                )
            }
        }
    }
}

@Composable
private fun ActivityHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorPaletteActivity.Ink),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ColorPaletteActivity.Ink, Color(0xFF231D3D))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Dragon Story Path",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Every productive rhythm becomes a timeless chronicle your companion carries forward.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun DayHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
            color = ColorPaletteActivity.Violet,
            modifier = Modifier
                .background(ColorPaletteActivity.Violet.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ActivityTimelineItem(
    event: GameEventEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reinforcementMessage = remember(event.id) {
        ReinforcementMessageProvider.timelineMessage(context, event)
    }

    val isMilestone = event.type == GameEventType.DRAGON_EVOLUTION.name ||
            event.type == GameEventType.LEVEL_UP.name ||
            event.type == GameEventType.STREAK_MILESTONE.name ||
            event.type == GameEventType.DAILY_GOAL_COMPLETED.name ||
            event.type == GameEventType.SURPRISE_REWARD.name ||
            event.type == GameEventType.COMBO_MILESTONE.name

    val accentColor = rarityColor(event.rarity)

    IntrinsicSizeRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            TimelineNode(accent = accentColor)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(ColorPaletteActivity.Line)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        TimelineEventCard(
            event = event,
            reinforcementMessage = reinforcementMessage,
            isMilestone = isMilestone,
            accent = accentColor,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        )
    }
}

@Composable
private fun IntrinsicSizeRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier = modifier.height(IntrinsicSize.Min), content = content)
}

@Composable
private fun TimelineNode(accent: Color) {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(accent.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.White, CircleShape)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun TimelineEventCard(
    event: GameEventEntity,
    reinforcementMessage: String,
    isMilestone: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMilestone) accent.copy(alpha = 0.04f) else ColorPaletteActivity.Card
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isMilestone) accent.copy(alpha = 0.3f) else ColorPaletteActivity.Line.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMilestone) 2.dp else 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accent.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForEvent(event.type),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorPaletteActivity.Ink
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = accent.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = rewardPreview(event).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                color = accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = timeAgo(event.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = ColorPaletteActivity.Muted
                )
            }

            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = ColorPaletteActivity.Ink.copy(alpha = 0.75f),
                lineHeight = 18.sp
            )

            if (reinforcementMessage.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ColorPaletteActivity.Amber.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💬 $reinforcementMessage",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = ColorPaletteActivity.AmberText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

private fun groupEventsByDay(events: List<GameEventEntity>): List<TimelineGroup> {
    val today = dayKey(System.currentTimeMillis())
    val yesterday = dayKey(System.currentTimeMillis() - DAY_MILLIS)
    val grouped = linkedMapOf<String, MutableList<GameEventEntity>>()

    events.forEach { event ->
        val label = when (dayKey(event.timestamp)) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> "Earlier Chronicles"
        }
        grouped.getOrPut(label) { mutableListOf() }.add(event)
    }

    return grouped.map { (label, dayEvents) -> TimelineGroup(label, dayEvents) }
}

private fun dayKey(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
}

private fun timeAgo(timestamp: Long): String {
    val diff = (System.currentTimeMillis() - timestamp).coerceAtLeast(0)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diff < minute -> "Just now"
        diff < hour -> "${diff / minute}m ago"
        diff < day -> "${diff / hour}h ago"
        else -> "${diff / day}d ago"
    }
}

private fun rewardPreview(event: GameEventEntity): String = when (event.type) {
    GameEventType.HABIT_COMPLETED.name -> "Habit Reward"
    GameEventType.ACHIEVEMENT_UNLOCKED.name -> "Milestone Loot"
    GameEventType.LEVEL_UP.name -> "Level Up Tier"
    GameEventType.DRAGON_EVOLUTION.name -> "Evolution Moment"
    GameEventType.CHEST_OPENED.name -> "Treasure Unlock"
    GameEventType.STREAK_MILESTONE.name -> "Streak Reward"
    GameEventType.DAILY_GOAL_COMPLETED.name -> "Daily Bonus"
    GameEventType.SURPRISE_REWARD.name -> "Surprise Buff"
    GameEventType.COMBO_MILESTONE.name -> "Momentum Buff"
    GameEventType.FIRST_DAILY_LOGIN.name -> "Daily Welcome"
    else -> "Progress Memory"
}

private fun iconForEvent(type: String) = when (type) {
    GameEventType.HABIT_COMPLETED.name -> Icons.Default.CheckCircle
    GameEventType.ACHIEVEMENT_UNLOCKED.name -> Icons.Default.EmojiEvents
    GameEventType.LEVEL_UP.name -> Icons.Default.Star
    GameEventType.DRAGON_EVOLUTION.name -> Icons.Default.Pets
    GameEventType.CHEST_OPENED.name -> Icons.Default.CardGiftcard
    GameEventType.STREAK_MILESTONE.name -> Icons.Default.FavoriteBorder
    GameEventType.DAILY_GOAL_COMPLETED.name -> Icons.Default.CheckCircle
    GameEventType.SURPRISE_REWARD.name -> Icons.Default.Star
    GameEventType.COMBO_MILESTONE.name -> Icons.Default.Star
    GameEventType.FIRST_DAILY_LOGIN.name -> Icons.Default.Book
    else -> Icons.Default.Star
}

@Composable
private fun rarityColor(rarity: String): Color = when (rarity) {
    GameEventRarity.LEGENDARY.name -> Color(0xFFFF9F1C)
    GameEventRarity.EPIC.name -> Color(0xFFA14BFF)
    GameEventRarity.RARE.name -> Color(0xFF3B91FF)
    else -> Color(0xFF6F6A8A)
}

private data class TimelineGroup(
    val label: String,
    val events: List<GameEventEntity>
)

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

private object ColorPaletteActivity {
    val Card = Color(0xFFFFFFFF)
    val Violet = Color(0xFF8A76F9)
    val Flame = Color(0xFFFF6B35)
    val Amber = Color(0xFFFFB84D)
    val AmberText = Color(0xFFB37400)
    val Line = Color(0xFFEBE9F5)
    val Muted = Color(0xFF8E8A9F)
    val Ink = Color(0xFF1E1A34)
}