package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.data.local.entities.GameEventEntity
import com.example.mobile.domain.GameEventType
import com.example.mobile.domain.GameEventRarity
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ProgressHeader
import com.example.mobile.presentation.ui.components.ProgressHeaderState
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
    val error by activityTimelineViewModel.error.collectAsState()
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
        topBar = {
            androidx.compose.material3.CenterAlignedTopAppBar(
                title = { Text("Activity", color = ColorPaletteActivity.Ink) }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ActivityHeader(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
                ProgressHeader(
                    state = ProgressHeaderState(
                        level = progressUiState.pet.level,
                        xp = progressUiState.pet.xp,
                        evolutionStage = progressUiState.pet.evolutionStage,
                        totalCoins = progressUiState.totalCoins,
                        globalStreak = progressUiState.globalStreak,
                        currentCombo = progressUiState.currentCombo,
                        lastHabitCompletionTimestamp = progressUiState.lastHabitCompletionTimestamp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            when {
                isLoading && events.isEmpty() -> item {
                    Text(
                        text = "Loading your dragon's story...",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                !error.isNullOrBlank() -> item {
                    Text(
                        text = error.orEmpty(),
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                events.isEmpty() -> item {
                    EmptyStateCard(
                        title = "Your story begins here",
                        message = "Complete one small habit today and your dragon will start remembering your journey.",
                        hint = "Your next habit completion will become the first memory in this timeline.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            groups.forEach { group ->
                item {
                    DayHeader(label = group.label)
                }

                items(
                    items = group.events,
                    key = { it.id }
                ) { event ->
                    val index = events.indexOf(event)
                    ActivityTimelineItem(
                        event = event,
                        isLeft = index % 2 == 0,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
            }

            if (hasMore) {
                item {
                    Button(
                        onClick = { activityTimelineViewModel.loadMore() },
                        enabled = !isLoadingMore,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPaletteActivity.Violet),
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        if (isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLoadingMore) "Loading older memories" else "Load older memories")
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityHeader(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorPaletteActivity.Card),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorPaletteActivity.LavenderSoft)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dragon story path",
                    style = MaterialTheme.typography.headlineSmall,
                    color = ColorPaletteActivity.Ink
                )
                Text(
                    text = "Every habit, reward, and milestone becomes a memory your dragon carries forward.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayHeader(label: String) {
    Surface(
        modifier = Modifier.padding(vertical = 10.dp),
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = ColorPaletteActivity.Violet,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ActivityTimelineItem(
    event: GameEventEntity,
    isLeft: Boolean,
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
    val accent = rarityColor(event.rarity)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLeft) {
            TimelineEventCard(
                event = event,
                reinforcementMessage = reinforcementMessage,
                isMilestone = isMilestone,
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        TimelineNode(accent = accent)

        if (!isLeft) {
            TimelineEventCard(
                event = event,
                reinforcementMessage = reinforcementMessage,
                isMilestone = isMilestone,
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TimelineNode(accent: Color) {
    Box(
        modifier = Modifier
            .size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(ColorPaletteActivity.Line, RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(accent, RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(Color.White, RoundedCornerShape(999.dp))
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
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isMilestone) accent.copy(alpha = 0.12f) else ColorPaletteActivity.Card
        ),
        shape = RoundedCornerShape(if (isMilestone) 26.dp else 22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMilestone) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconForEvent(event.type),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(if (isMilestone) 30.dp else 24.dp)
                )
                Text(
                    text = timeAgo(event.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = event.title,
                style = if (isMilestone) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                color = ColorPaletteActivity.Ink
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = reinforcementMessage,
                style = MaterialTheme.typography.labelMedium,
                color = ColorPaletteActivity.Amber
            )
            Text(
                text = rewardPreview(event),
                style = MaterialTheme.typography.labelMedium,
                color = accent
            )
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
            else -> "Earlier"
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
    GameEventType.HABIT_COMPLETED.name -> "Habit reward"
    GameEventType.ACHIEVEMENT_UNLOCKED.name -> "Achievement milestone"
    GameEventType.LEVEL_UP.name -> "Level-up reward"
    GameEventType.DRAGON_EVOLUTION.name -> "Evolution moment"
    GameEventType.CHEST_OPENED.name -> "Chest reward"
    GameEventType.STREAK_MILESTONE.name -> "Streak chest"
    GameEventType.DAILY_GOAL_COMPLETED.name -> "Daily goal reward"
    GameEventType.SURPRISE_REWARD.name -> "Surprise bonus"
    GameEventType.COMBO_MILESTONE.name -> "Momentum bonus"
    GameEventType.FIRST_DAILY_LOGIN.name -> "Daily welcome"
    else -> "Progress memory"
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
    GameEventRarity.LEGENDARY.name -> Color(0xFFFFD700)
    GameEventRarity.EPIC.name -> Color(0xFFA855F7)
    GameEventRarity.RARE.name -> Color(0xFF60A5FA)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private data class TimelineGroup(
    val label: String,
    val events: List<GameEventEntity>
)

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

private object ColorPaletteActivity {
    val Card = Color(0xFFFFFFFF)
    val LavenderSoft = Color(0xFFF2EEFF)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Line = Color(0xFFE5DDFF)
    val Ink = Color(0xFF302B4A)
}
