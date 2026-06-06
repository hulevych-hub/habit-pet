package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.presentation.viewmodel.JournalViewModel


/**
 * Simple screen for displaying journal entries
 */
@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Journal Screen",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}