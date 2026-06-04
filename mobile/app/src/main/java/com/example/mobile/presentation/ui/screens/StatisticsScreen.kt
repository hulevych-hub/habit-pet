package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun StatisticsScreen(statisticsViewModel: StatisticsViewModel = hiltViewModel()) {
    val stats by statisticsViewModel.statistics.collectAsState(initial = StatisticsEntity())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Statistics") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            Text("Overall Statistics", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            StatRow("Current Streak", "${stats.currentStreak} days")
            StatRow("Best Streak", "${stats.bestStreak} days")
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            StatRow("Total Completions", "${stats.totalCompletions}")
            StatRow("Total XP", "${stats.totalXp}")
            StatRow("Days Active", "${stats.daysActive}")
            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            StatRow("Total Habits Completed", "${stats.totalHabitsCompleted}")
            StatRow("Pet Age", "${stats.petAgeDays} days")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
    }
}
