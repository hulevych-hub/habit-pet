package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import com.example.mobile.presentation.viewmodel.HabitCreationViewModel
import com.example.mobile.ui.theme.HabitPetTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HabitCreationScreen(
    onNavigateUp: () -> Unit,
    onHabitCreated: () -> Unit
) {
    val viewModel: HabitCreationViewModel = hiltViewModel()
    LaunchedEffect(viewModel) {
        viewModel.habitCreated.collect {
            onHabitCreated()
        }
    }

    HabitPetTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Create Habit") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            HabitCreationForm(
                viewModel = viewModel,
                onHabitCreated = onHabitCreated,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun HabitCreationForm(
    viewModel: HabitCreationViewModel,
    onHabitCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name by viewModel.name.collectAsState()
    val icon by viewModel.icon.collectAsState()
    val type by viewModel.type.collectAsState()
    val minimumDuration by viewModel.minimumDuration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = modifier
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

        // Create Button
        Button(
            onClick = {
                viewModel.createHabit()
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create Habit")
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
            val iconList = listOf("❤️", "🎯", "📚", "💪", "🧘", "📝", "🍎", "🚴")
            iconList.forEach { icon ->
                IconButton(
                    onClick = { onIconSelected(icon) },
                    enabled = true,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (selectedIcon == icon)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
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
