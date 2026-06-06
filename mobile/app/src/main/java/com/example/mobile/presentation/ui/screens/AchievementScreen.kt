package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobile.presentation.viewmodel.AchievementViewModel

/**
 * Screen for displaying achievements
 */
@Composable
fun AchievementScreen(
    viewModel: AchievementViewModel = viewModel()
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

        // Loading state
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(alignment = androidx.compose.ui.Alignment.CenterHorizontally)
            )
        } else {
            // Error state
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // Empty state
                if (achievements.isEmpty()) {
                    Text(
                        text = "No achievements yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    // Achievements list - simplified for now
                    Text(
                        text = "Achievements: ${achievements.size}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

