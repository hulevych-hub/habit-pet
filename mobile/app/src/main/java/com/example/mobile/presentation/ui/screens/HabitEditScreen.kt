package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.presentation.viewmodel.HabitEditViewModel
import com.example.mobile.ui.theme.HabitPetTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HabitEditScreen(
    habitId: Long,
    viewModel: HabitEditViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    onHabitUpdated: () -> Unit,
    onHabitDeleted: () -> Unit
) {
    LaunchedEffect(habitId) {
        viewModel.initialize(habitId)
    }
    LaunchedEffect(viewModel) {
        viewModel.habitUpdated.collect {
            onHabitUpdated()
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.habitDeleted.collect {
            onHabitDeleted()
        }
    }

    HabitPetTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Edit Habit") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            SingleLineSpecifiedPadding(padding) {
                HabitEditForm(
                    viewModel = viewModel,
                    habitId = habitId,
                    onHabitUpdated = onHabitUpdated,
                    onHabitDeleted = onHabitDeleted
                )
            }
        }
    }
}

@Composable
private fun HabitEditForm(
    viewModel: HabitEditViewModel,
    habitId: Long,
    onHabitUpdated: () -> Unit,
    onHabitDeleted: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val icon by viewModel.icon.collectAsState()
    val type by viewModel.type.collectAsState()
    val minimumDuration by viewModel.minimumDuration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.onNameChanged(it) },
            label = { Text("Habit Name") },
            placeholder = { Text("Enter habit name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        // Icon Selection
        IconSelection(
            selectedIcon = icon,
            onIconSelected = { viewModel.onIconSelected(it) }
        )

        // Type Selection
        TypeSelection(
            selectedType = type,
            onTypeSelected = { viewModel.onTypeSelected(it) }
        )

        // Minimum Duration (only for timer habits)
        if (type == "TIMER") {
            DurationSelection(
                duration = minimumDuration,
                onDurationChanged = { viewModel.onMinimumDurationChanged(it) }
            )
        }

        // Error Message
        error?.let {
            Text(
                text = it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Update Button
            Button(
                onClick = {
                    viewModel.updateHabit()
                },
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update Habit")
                }
            }

            // Delete Button
            Button(
                onClick = {
                    viewModel.deleteHabit()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Delete Habit", color = Color.Red)
            }
        }
    }
}

@Composable
private fun IconSelection(
    selectedIcon: String,
    onIconSelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Icon", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Simple icon selector - in a real app, this would show actual icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val icons = listOf("❤️", "🎯", "📚", "💪", "🧘", "📝", "🍎", "🚴")
            icons.forEach { icon ->
                IconButton(
                    onClick = { onIconSelected(icon) },
                    enabled = true,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (selectedIcon == icon)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(icon, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun TypeSelection(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Habit Type", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TypeChoiceChip(
                label = "Checkbox",
                value = "CHECKBOX",
                selected = selectedType == "CHECKBOX",
                onSelect = { onTypeSelected(it) }
            )
            TypeChoiceChip(
                label = "Timer",
                value = "TIMER",
                selected = selectedType == "TIMER",
                onSelect = { onTypeSelected(it) }
            )
        }
    }
}

@Composable
private fun TypeChoiceChip(
    label: String,
    value: String,
    selected: Boolean,
    onSelect: (String) -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = { onSelect(value) },
        label = { Text(label) }
    )
}

@Composable
private fun DurationSelection(
    duration: Int,
    onDurationChanged: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Minimum Duration (minutes)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$duration min", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(8.dp))

            // Simple duration picker - in a real app, this might be a slider or number picker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onDurationChanged((duration - 5).coerceAtLeast(0)) },
                    enabled = duration > 0
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease duration")
                }
                IconButton(
                    onClick = { onDurationChanged(duration + 5) },
                    enabled = duration < 120 // Max 2 hours
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase duration")
                }
            }
        }
    }
}

@Composable
private fun SingleLineSpecifiedPadding(
    innerPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        content()
    }
}
