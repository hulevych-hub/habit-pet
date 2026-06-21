package com.example.mobile.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mobile.data.local.entities.HabitEntity
import com.example.mobile.domain.repository.HabitCompletionRepository
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.HabitPetTheme
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

enum class StreakCalendarDayStatus {
    EMPTY,
    COMPLETED,
    FREEZE
}

private enum class StreakCalendarNavigationDirection {
    NONE,
    PREVIOUS,
    NEXT
}

data class StreakCalendarDay(
    val date: Long,
    val dayOfMonth: Int,
    val status: StreakCalendarDayStatus,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

data class StreakCalendarUiState(
    val title: String,
    val subtitle: String,
    val monthStart: Long,
    val days: List<StreakCalendarDay>,
    val isLoading: Boolean = false,
    val canNavigatePrevious: Boolean = true,
    val canNavigateNext: Boolean = true,
    val showFreezeLegend: Boolean = true,
    val showTitleSubtitle: Boolean = true
) {
    companion object {
        fun loading(title: String, subtitle: String, monthStart: Long): StreakCalendarUiState =
            StreakCalendarUiState(
                title = title,
                subtitle = subtitle,
                monthStart = monthStart,
                days = emptyList(),
                isLoading = true,
                canNavigatePrevious = monthStart <= getMonthStart(System.currentTimeMillis()),
                canNavigateNext = monthStart < getMonthStart(System.currentTimeMillis()),
                showFreezeLegend = true
            )
    }
}

object StreakCalendarBuilder {
    suspend fun buildGlobal(
        monthStart: Long,
        habits: List<HabitEntity>,
        completionRepository: HabitCompletionRepository,
        frozenDates: Set<Long> = emptySet()
    ): StreakCalendarUiState {
        val nextMonthStart = addMonths(monthStart, 1)
        val completionsByHabit = habits.associate { habit ->
            habit.id to completionRepository
                .getCompletionsForHabit(habit.id, monthStart, nextMonthStart)
                .firstOrNull()
                .orEmpty()
        }
        val completionCountByDate = mutableMapOf<Long, Int>()
        completionsByHabit.values.flatten().forEach { completion ->
            completionCountByDate[completion.date] = (completionCountByDate[completion.date] ?: 0) + 1
        }

        val days = buildDays(monthStart) { date, _, isToday, isFuture ->
            val dateKey = date / 86_400_000L
            when {
                isFuture || habits.isEmpty() -> StreakCalendarDayStatus.EMPTY
                completionCountByDate[date] == habits.size -> StreakCalendarDayStatus.COMPLETED
                frozenDates.contains(dateKey) -> StreakCalendarDayStatus.FREEZE
                isToday -> StreakCalendarDayStatus.EMPTY
                else -> StreakCalendarDayStatus.EMPTY
            }
        }

        return StreakCalendarUiState(
            title = "Global Streak",
            subtitle = if (habits.isEmpty()) {
                "Create a habit to start warming up your streak flame."
            } else {
                "Fire = every habit complete. Cold fire = frozen streak day."
            },
            monthStart = monthStart,
            days = days,
            canNavigatePrevious = monthStart <= getMonthStart(System.currentTimeMillis()),
            canNavigateNext = monthStart < getMonthStart(System.currentTimeMillis()),
            showFreezeLegend = habits.isNotEmpty(),
            showTitleSubtitle = false
        )
    }

    suspend fun buildHabit(
        monthStart: Long,
        habit: HabitEntity,
        completionRepository: HabitCompletionRepository
    ): StreakCalendarUiState {
        val nextMonthStart = addMonths(monthStart, 1)
        val completions = completionRepository
            .getCompletionsForHabit(habit.id, monthStart, nextMonthStart)
            .firstOrNull()
            .orEmpty()
        val completedDates = completions.map { it.date }.toSet()

        val days = buildDays(monthStart) { date, _, _, isFuture ->
            if (isFuture) {
                StreakCalendarDayStatus.EMPTY
            } else if (completedDates.contains(date)) {
                StreakCalendarDayStatus.COMPLETED
            } else {
                StreakCalendarDayStatus.EMPTY
            }
        }

        return StreakCalendarUiState(
            title = habit.name.ifBlank { "Habit Streak" },
            subtitle = "Fire = ${habit.name.ifBlank { "this habit" }} completed on that day.",
            monthStart = monthStart,
            days = days,
            canNavigatePrevious = monthStart <= getMonthStart(System.currentTimeMillis()),
            canNavigateNext = monthStart < getMonthStart(System.currentTimeMillis()),
            showFreezeLegend = false
        )
    }

    private fun buildDays(
        monthStart: Long,
        statusFor: (Long, Boolean, Boolean, Boolean) -> StreakCalendarDayStatus
    ): List<StreakCalendarDay> {
        val monthCalendar = Calendar.getInstance().apply {
            timeInMillis = monthStart
            normalizeDay()
        }
        val today = getDayStart(System.currentTimeMillis())
        val firstWeekday = monthCalendar.get(Calendar.DAY_OF_WEEK)
        val leadingDays = (firstWeekday - Calendar.SUNDAY + 7) % 7
        val days = mutableListOf<StreakCalendarDay>()

        repeat(42) { index ->
            val dayCalendar = Calendar.getInstance().apply {
                timeInMillis = monthStart
                add(Calendar.DAY_OF_MONTH, index - leadingDays)
                normalizeDay()
            }
            val date = dayCalendar.timeInMillis
            val isCurrentMonth = dayCalendar.get(Calendar.MONTH) == monthCalendar.get(Calendar.MONTH) &&
                dayCalendar.get(Calendar.YEAR) == monthCalendar.get(Calendar.YEAR)
            val isToday = date == today
            val isFuture = date > today

            days += StreakCalendarDay(
                date = date,
                dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                status = statusFor(date, isCurrentMonth, isToday, isFuture),
                isCurrentMonth = isCurrentMonth,
                isToday = isToday
            )
        }

        return days
    }
}

@Composable
fun StreakCalendarOverlay(
    state: StreakCalendarUiState?,
    onDismiss: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    if (state == null) return

    var navigationDirection by remember { mutableStateOf(StreakCalendarNavigationDirection.NONE) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = AppTheme.current.card,
            shadowElevation = 18.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(state.monthStart, state.isLoading) {
                        var dragDistance = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                dragDistance += dragAmount
                            },
                            onDragEnd = {
                                val threshold = 48.dp.toPx()
                                when {
                                    state.isLoading || abs(dragDistance) < threshold -> Unit
                                    dragDistance > 0 && state.canNavigatePrevious -> {
                                        navigationDirection = StreakCalendarNavigationDirection.PREVIOUS
                                        onPreviousMonth()
                                    }
                                    dragDistance < 0 && state.canNavigateNext -> {
                                        navigationDirection = StreakCalendarNavigationDirection.NEXT
                                        onNextMonth()
                                    }
                                }
                                dragDistance = 0f
                            }
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StreakCalendarControls(
                        title = state.title,
                        subtitle = state.subtitle,
                        showTitleSubtitle = state.showTitleSubtitle,
                        monthTitle = formatMonthYear(state.monthStart),
                        canNavigatePrevious = state.canNavigatePrevious,
                        canNavigateNext = state.canNavigateNext,
                        onPreviousMonth = {
                            navigationDirection = StreakCalendarNavigationDirection.PREVIOUS
                            onPreviousMonth()
                        },
                        onNextMonth = {
                            navigationDirection = StreakCalendarNavigationDirection.NEXT
                            onNextMonth()
                        }
                    )

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppTheme.current.violet)
                        }
                    } else {
                        AnimatedContent(
                            targetState = state.monthStart,
                            transitionSpec = {
                                when (navigationDirection) {
                                    StreakCalendarNavigationDirection.PREVIOUS -> {
                                        slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                                    }
                                    StreakCalendarNavigationDirection.NEXT -> {
                                        slideInHorizontally(initialOffsetX = { -it }) + fadeIn() togetherWith
                                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                                    }
                                    StreakCalendarNavigationDirection.NONE -> {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                }
                            },
                            label = "Streak calendar month"
                        ) {
                            StreakCalendarGrid(days = state.days)
                        }
                    }

                    StreakCalendarLegend(showFreezeLegend = state.showFreezeLegend)
                }
            }
        }
    }
}

@Composable
private fun StreakCalendarControls(
    title: String,
    subtitle: String,
    showTitleSubtitle: Boolean,
    monthTitle: String,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            enabled = canNavigatePrevious,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous month",
                tint = if (canNavigatePrevious) AppTheme.current.violet else AppTheme.current.muted,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.current.ink,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            if (showTitleSubtitle) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.current.ink,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.current.muted,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        IconButton(
            onClick = onNextMonth,
            enabled = canNavigateNext,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next month",
                tint = if (canNavigateNext) AppTheme.current.violet else AppTheme.current.muted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StreakCalendarGrid(days: List<StreakCalendarDay>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        WeekdayRow()
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { day ->
                    StreakCalendarDayCell(day = day)
                }
            }
        }
    }
}

@Composable
private fun WeekdayRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
        weekdays.forEach { weekday ->
            Text(
                text = weekday,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.current.muted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StreakCalendarDayCell(day: StreakCalendarDay) {
    val cellColor = when (day.status) {
        StreakCalendarDayStatus.COMPLETED -> AppTheme.current.amberSoft
        StreakCalendarDayStatus.FREEZE -> AppTheme.current.blueSoft
        StreakCalendarDayStatus.EMPTY -> AppTheme.current.surface.copy(alpha = 0.58f)
    }
    val contentAlpha = if (day.isCurrentMonth) 1f else 0.48f

    Surface(
        modifier = Modifier
            .fillMaxWidth(1f / 7f)
            .height(46.dp),
        shape = RoundedCornerShape(14.dp),
        color = cellColor,
        border = if (day.isToday) BorderStroke(1.dp, AppTheme.current.gold) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${day.dayOfMonth}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppTheme.current.ink.copy(alpha = contentAlpha)
                )

                when (day.status) {
                    StreakCalendarDayStatus.COMPLETED -> {
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Completed",
                            tint = AppTheme.current.amber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    StreakCalendarDayStatus.FREEZE -> {
                        Spacer(modifier = Modifier.height(2.dp))
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Frozen streak",
                            tint = AppTheme.current.blue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    StreakCalendarDayStatus.EMPTY -> Unit
                }
            }
        }
    }
}

@Composable
private fun StreakCalendarLegend(showFreezeLegend: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(
            icon = Icons.Default.LocalFireDepartment,
            label = "Completed",
            tint = AppTheme.current.amber
        )
        if (showFreezeLegend) {
            LegendItem(
                icon = Icons.Default.LocalFireDepartment,
                label = "Frozen streak",
                tint = AppTheme.current.blue
            )
        }
    }
}

@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.current.muted
        )
    }
}

private fun getMonthStart(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        normalizeDay()
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return calendar.timeInMillis
}

private fun formatMonthYear(timestamp: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        normalizeDay()
    }
    return "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US)} ${calendar.get(Calendar.YEAR)}"
}

private fun addMonths(monthStart: Long, months: Int): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = monthStart
        normalizeDay()
        add(Calendar.MONTH, months)
    }
    return calendar.timeInMillis
}

private fun getDayStart(timestamp: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timestamp
        normalizeDay()
    }
    return calendar.timeInMillis
}

private fun Calendar.normalizeDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun StreakCalendarOverlayPreview() {
    HabitPetTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.current.background)
        ) {
            StreakCalendarOverlay(
                state = StreakCalendarUiState(
                    title = "Global Streak",
                    subtitle = "Fire = every habit complete. Cold = partial activity day.",
                    monthStart = getMonthStart(System.currentTimeMillis()),
                    days = StreakCalendarBuilder.run {
                        buildDaysForPreview()
                    },
                    canNavigatePrevious = true,
                    canNavigateNext = true,
                    showFreezeLegend = true
                ),
                onDismiss = {},
                onPreviousMonth = {},
                onNextMonth = {}
            )
        }
    }
}

@Composable
private fun buildDaysForPreview(): List<StreakCalendarDay> {
    val today = getDayStart(System.currentTimeMillis())
    return (0 until 42).map { index ->
        val calendar = Calendar.getInstance().apply {
            timeInMillis = getMonthStart(System.currentTimeMillis())
            add(Calendar.DAY_OF_MONTH, index - 3)
            normalizeDay()
        }
        StreakCalendarDay(
            date = calendar.timeInMillis,
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
            status = when {
                index in 3..9 -> StreakCalendarDayStatus.COMPLETED
                index == 10 -> StreakCalendarDayStatus.FREEZE
                index in 12..15 -> StreakCalendarDayStatus.COMPLETED
                else -> StreakCalendarDayStatus.EMPTY
            },
            isCurrentMonth = calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH),
            isToday = calendar.timeInMillis == today
        )
    }
}
