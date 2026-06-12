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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.presentation.ui.components.AnimatedPet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {
    val pet = petRepository.getPet()

    fun renamePet(name: String, currentPet: PetEntity) = viewModelScope.launch {
        petRepository.updatePet(currentPet.copy(id = currentPet.id, name = name))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PetScreen(
    petViewModel: PetViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    // 1. Get the unified UI state from the HomeScreenViewModel
    val uiState by homeScreenViewModel.uiState.collectAsState()

    // 2. Extract the safe, fully-loaded pet data (ID = 1)
    val pet = uiState.pet

    var showRenameDialog by remember { mutableStateOf(false) }

    val currentLevelXp = ExpConfig.xpProgressInCurrentLevel(pet.xp)
    val xpRequiredForNextLevel = ExpConfig.xpRequiredForCurrentLevelProgress(pet.xp)
    val progressFraction = (currentLevelXp.toFloat() / xpRequiredForNextLevel.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        containerColor = ColorPalettePet.BackgroundColor,
        topBar = {
            GamifiedFixedHeader(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(pet.evolutionStage)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP SECTION: Clean minimal dragon name header with inline edit action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pet.name.ifBlank { "Baby Dragon" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = ColorPalettePet.Ink
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename Pet",
                        tint = ColorPalettePet.Violet,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. CENTER SECTION: Ultimate Showcase Hero Area (Isolated Alpha Layers)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Radial Glow layer isolated away from drawing context of pet model
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ColorPalettePet.Violet.copy(alpha = 0.15f),
                                    ColorPalettePet.BackgroundColor.copy(alpha = 0.0f)
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                )

                // High fidelity fully opaque character layout
                AnimatedPet(
                    pet = pet,
                    modifier = Modifier.size(340.dp)
                )
            }

            // 3. BOTTOM SECTION: Clean, modern floating card containing stats & live cosmetics status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                colors = CardDefaults.cardColors(containerColor = ColorPalettePet.CardBackground),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Level Indicator + Mood Pill Line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(shape = RoundedCornerShape(999.dp), color = ColorPalettePet.Amber) {
                                Text(
                                    text = "Lv. ${pet.level}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            Text(
                                text = "Track Progress",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ColorPalettePet.Ink
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = ColorPalettePet.MintSoft
                        ) {
                            Text(
                                text = DragonMood.from(pet.mood).displayName,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ColorPalettePet.Green,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Compact Experience Meter Line
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Experience Points",
                                style = MaterialTheme.typography.labelMedium,
                                color = ColorPalettePet.Muted
                            )
                            Text(
                                text = "$currentLevelXp / $xpRequiredForNextLevel XP",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = ColorPalettePet.Ink
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = ColorPalettePet.Violet,
                            trackColor = ColorPalettePet.ProgressTrack
                        )
                    }

                    HorizontalDivider(
                        color = ColorPalettePet.Ink.copy(alpha = 0.06f),
                        thickness = 1.dp
                    )

                    // Equipping Layout Cosmetics Feed Summaries
                    CustomizationSummary(
                        outfit = pet.equippedOutfit,
                        background = pet.equippedBackground,
                        aura = pet.equippedAura
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        PetRenameDialog(
            initialName = pet.name,
            onDismissRequest = { showRenameDialog = false },
            onConfirm = { newName ->
                petViewModel.renamePet(newName.trim(), pet)
                showRenameDialog = false
            }
        )
    }
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = ColorPalettePet.Honey,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$streak d",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPalettePet.Ink
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ColorPalettePet.Violet.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = ColorPalettePet.Violet,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stageName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ColorPalettePet.Violet
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Coins",
                    tint = ColorPalettePet.Amber,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "$coins",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ColorPalettePet.Ink
                )
            }
        }
    }
}

@Composable
private fun CustomizationSummary(
    outfit: String?,
    background: String?,
    aura: String?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ColorPalettePet.Violet, modifier = Modifier.size(16.dp))
                Text("Equipment Wardrobe", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = ColorPalettePet.Ink)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryLine("Outfit Slot", outfit)
            SummaryLine("Scene Background", background)
            SummaryLine("Active Aura", aura)
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = ColorPalettePet.Muted)
        Text(
            text = value ?: "None Equipped",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = if (value != null) ColorPalettePet.Violet else ColorPalettePet.Muted.copy(alpha = 0.6f)
        )
    }
}

private const val MAX_PET_NAME_LENGTH = 24

internal fun validatePetName(name: String): String? {
    val trimmedName = name.trim()
    return when {
        trimmedName.isEmpty() -> "Please enter a pet name"
        trimmedName.length > MAX_PET_NAME_LENGTH -> "Pet name must be $MAX_PET_NAME_LENGTH characters or fewer"
        else -> null
    }
}

@Composable
internal fun PetRenameDialog(
    initialName: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
    helperText: String? = null,
    allowDismiss: Boolean = true
) {
    var nameDraft by remember { mutableStateOf(initialName) }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (allowDismiss) onDismissRequest() },
        title = { Text("Rename Pet") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = {
                        nameDraft = it
                        nameError = null
                    },
                    label = { Text("Pet name") },
                    placeholder = { Text("Enter pet name") },
                    singleLine = true,
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth()
                )
                helperText?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                nameError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "${nameDraft.length}/$MAX_PET_NAME_LENGTH",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = ColorPalettePet.Violet),
                onClick = {
                    val error = validatePetName(nameDraft)
                    if (error == null) {
                        onConfirm(nameDraft)
                    } else {
                        nameError = error
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            if (allowDismiss) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel", color = ColorPalettePet.Muted)
                }
            }
        }
    )
}

private object ColorPalettePet {
    val BackgroundColor = Color(0xFFFAFAFC)
    val CardBackground = Color(0xFFFFFFFF)
    val ProgressTrack = Color(0xFFEBE6FC)
    val Violet = Color(0xFF8A76F9)
    val Amber = Color(0xFFFFB84D)
    val Honey = Color(0xFFFF9F1C)
    val MintSoft = Color(0xFFE5F9EE)
    val Green = Color(0xFF23A160)
    val Muted = Color(0xFF6A6581)
    val Ink = Color(0xFF1E1A34)
}