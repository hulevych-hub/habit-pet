package com.example.mobile.presentation.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.presentation.viewmodel.ActivityTimelineViewModel

@Composable
fun JournalScreen(
    activityTimelineViewModel: ActivityTimelineViewModel = hiltViewModel()
) {
    ActivityTimelineScreen(activityTimelineViewModel = activityTimelineViewModel)
}
