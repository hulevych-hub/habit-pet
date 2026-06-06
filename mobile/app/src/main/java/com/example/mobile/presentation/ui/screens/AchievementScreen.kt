package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.presentation.viewmodel.AchievementViewModel

@Composable
fun AchievementScreen(
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            achievements.isEmpty() -> {
                Text("No achievements yet")
            }

            else -> {
                val unlocked = achievements.filter { it.isUnlocked }
                val locked = achievements.filter { !it.isUnlocked }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // UNLOCKED SECTION
                    if (unlocked.isNotEmpty()) {
                        item {
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                        }

                        items(unlocked) { achievement ->
                            AchievementItem(achievement)
                        }
                    }

                    // LOCKED SECTION
                    if (locked.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Locked",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Divider()
                        }

                        items(locked) { achievement ->
                            AchievementItem(achievement)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: com.example.mobile.data.local.entities.AchievementEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = if (achievement.isUnlocked)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Lock,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(text = achievement.description)

                Text(
                    text = "Reward: ${achievement.rewardCoins} coins",
                    style = MaterialTheme.typography.bodySmall
                )

                if (achievement.isUnlocked && achievement.unlockedDate != null) {
                    Text(
                        text = "Unlocked: ${achievement.unlockedDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}