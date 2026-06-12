package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.presentation.viewmodel.HabitCreationViewModel
import com.example.mobile.ui.theme.HabitPetTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
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
            containerColor = Color(0xFFFAFAFC),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "New Quest Blueprint",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ColorPaletteCreate.Ink
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = ColorPaletteCreate.Ink
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                HabitCreationForm(viewModel = viewModel)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HabitCreationForm(
    viewModel: HabitCreationViewModel,
    modifier: Modifier = Modifier
) {
    val name by viewModel.name.collectAsState()
    val icon by viewModel.icon.collectAsState()
    val type by viewModel.type.collectAsState()
    val minimumDuration by viewModel.minimumDuration.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorPaletteCreate.Card),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaletteCreate.Line.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g., Deep Work, Morning Run") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorPaletteCreate.Violet,
                        unfocusedBorderColor = ColorPaletteCreate.Line,
                        focusedLabelColor = ColorPaletteCreate.Violet,
                        unfocusedLabelColor = ColorPaletteCreate.Muted
                    )
                )
            }
        }

        // Expanded Icon Picker Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorPaletteCreate.Card),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaletteCreate.Line.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Visual Totem Symbol",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteCreate.Ink
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorPaletteCreate.Line.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .clickable { showBottomSheet = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, ColorPaletteCreate.Line, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(icon.ifBlank { HabitCreationViewModel.DEFAULT_ICON }, fontSize = 24.sp)
                        }
                        Column {
                            Text(
                                text = "Selected Emblem",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ColorPaletteCreate.Ink
                            )
                            Text(
                                text = "Default target icon ready • tap to change",
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorPaletteCreate.Muted
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Icon",
                        tint = ColorPaletteCreate.Violet,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Type Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorPaletteCreate.Card),
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaletteCreate.Line.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Verification Method",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = ColorPaletteCreate.Ink
                )
                Spacer(modifier = Modifier.height(12.dp))
                TypeSelection(
                    selectedType = type,
                    onTypeSelected = { viewModel.onTypeSelected(it) }
                )
            }
        }

        // Minimum Duration Card (Conditional block)
        if (type == "TIMER") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorPaletteCreate.Card),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaletteCreate.Line.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Minimum Activation Duration",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteCreate.Ink
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    DurationSelection(
                        duration = minimumDuration,
                        onDurationChanged = { viewModel.onMinimumDurationChanged(it) }
                    )
                }
            }
        }

        // Error Feedback Alert Box
        error?.let {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ColorPaletteCreate.Coral.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaletteCreate.Coral.copy(alpha = 0.2f))
            ) {
                Text(
                    text = it,
                    color = ColorPaletteCreate.Coral,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Premium Bottom Action Primary Trigger
        Button(
            onClick = { viewModel.createHabit() },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorPaletteCreate.Violet,
                disabledContainerColor = ColorPaletteCreate.Violet.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Construct Habit Module",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }
        }
    }

    // Modern Bottom Sheet Emoji Vault
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = Color.White,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .background(ColorPaletteCreate.Line, CircleShape)
                )
            },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            EmojiVaultContent(
                selectedEmoji = icon,
                onEmojiSelected = { chosenEmoji ->
                    viewModel.onIconSelected(chosenEmoji)
                    showBottomSheet = false
                }
            )
        }
    }
}

@Composable
private fun EmojiVaultContent(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojiCategories = remember {
        listOf(
            "Mind & Health" to listOf("🧘", "🧠", "❤️", "💪", "🥗", "🍎", "💧", "💊", "😴", "🥦", "🚶", "🏃"),
            "Focus & Progress" to listOf("🎯", "📚", "📝", "💻", "🎨", "🚀", "⏳", "💼", "📈", "🔥", "⚙️", "🧩"),
            "Routine & Hobbies" to listOf("🚴", "🎸", "🌱", "🧹", "⏰", "💵", "🔑", "🛁", "🍵", "🐾", "📸", "🎧"),
            "Rewards & Spirit" to listOf("⭐", "👑", "🏆", "💎", "✨", "🌟", "🍀", "🎉", "🌈", "☀️", "🌙", "🔮")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select Blueprint Icon",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = ColorPaletteCreate.Ink
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            emojiCategories.forEach { (categoryName, emojis) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPaletteCreate.Muted
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(88.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        userScrollEnabled = false
                    ) {
                        items(emojis) { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(
                                        color = if (isSelected) ColorPaletteCreate.Violet.copy(alpha = 0.12f) else ColorPaletteCreate.Line.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ColorPaletteCreate.Violet else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onEmojiSelected(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun TypeSelection(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("CHECKBOX" to "Simple Check", "TIMER" to "Countdown Timer").forEach { (value, label) ->
            val isSelected = selectedType == value
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTypeSelected(value) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) ColorPaletteCreate.Violet.copy(alpha = 0.04f) else Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (isSelected) ColorPaletteCreate.Violet else ColorPaletteCreate.Line.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) ColorPaletteCreate.Violet else ColorPaletteCreate.Ink
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationSelection(
    duration: Int,
    onDurationChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorPaletteCreate.Line.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = { onDurationChanged((duration - 5).coerceAtLeast(1)) },
            enabled = duration > 5,
            modifier = Modifier
                .background(Color.White, CircleShape)
                .size(36.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = ColorPaletteCreate.Ink, modifier = Modifier.size(18.dp))
        }

        Text(
            text = "$duration minutes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ColorPaletteCreate.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = { onDurationChanged(duration + 5) },
            enabled = duration < 180,
            modifier = Modifier
                .background(Color.White, CircleShape)
                .size(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = ColorPaletteCreate.Ink, modifier = Modifier.size(18.dp))
        }
    }
}

private object ColorPaletteCreate {
    val Card = Color(0xFFFFFFFF)
    val Violet = Color(0xFF8A76F9)
    val Line = Color(0xFFEBE9F5)
    val Muted = Color(0xFF8E8A9F)
    val Coral = Color(0xFFE65C5C)
    val Ink = Color(0xFF1E1A34)
}