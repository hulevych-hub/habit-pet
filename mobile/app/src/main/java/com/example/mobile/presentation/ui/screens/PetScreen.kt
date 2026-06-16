package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.presentation.ui.components.AnimatedPet
import com.example.mobile.presentation.ui.components.CoinIcon
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.LoadingStateCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.mobile.R
import javax.inject.Inject

private val MedallionSize = 118.dp

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val pet = petRepository.getPet()

    fun clearError() {
        _error.value = null
    }

    fun renamePet(name: String, currentPet: PetEntity) = viewModelScope.launch {
        _error.value = null
        try {
            petRepository.updatePet(currentPet.copy(id = currentPet.id, name = name))
        } catch (e: Exception) {
            _error.value = e.message ?: "Pet name could not be saved"
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PetScreen(
    petViewModel: PetViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToRewardsLocked: () -> Unit,
    onNavigateToRewardsOwned: () -> Unit
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val isLoading by homeScreenViewModel.isLoading.collectAsState()
    val error by petViewModel.error.collectAsState(initial = null)

    val pet = uiState.pet
    var showRenameDialog by remember { mutableStateOf(false) }

    val currentLevelXp = ExpConfig.xpProgressInCurrentLevel(pet.xp)
    val xpRequiredForNextLevel = ExpConfig.xpRequiredForCurrentLevelProgress(pet.xp)
    val progressFraction = (currentLevelXp.toFloat() / xpRequiredForNextLevel.toFloat()).coerceIn(0f, 1f)

    Scaffold(
        containerColor = PetPremiumColors.Background,
        topBar = {
            GamifiedFixedHeader(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(pet.evolutionStage),
                streakCompletedToday = uiState.globalStreakCompletedToday,
                onCoinsClick = onNavigateToRewardsLocked
            )
        }
    ) { padding ->
        if (!error.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                message = error.orEmpty(),
                onRetry = petViewModel::clearError
            )
        } else if (isLoading) {
            LoadingStateCard(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                message = "Checking on your dragon..."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PetPremiumColors.Background)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PetShowcase(
                    pet = pet,
                    level = pet.level,
                    name = pet.name.ifBlank { "Baby Dragon" },
                    mood = DragonMood.from(pet.mood).displayName,
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                PetDetailsPanel(
                    pet = pet,
                    progressFraction = progressFraction,
                    currentLevelXp = currentLevelXp,
                    xpRequiredForNextLevel = xpRequiredForNextLevel,
                    onRenameClick = { showRenameDialog = true },
                    onEditCustomizationsClick = onNavigateToRewardsOwned
                )

                Spacer(modifier = Modifier.height(24.dp))
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
private fun PetShowcase(
    pet: PetEntity,
    level: Int,
    name: String,
    mood: String,
    modifier: Modifier = Modifier
) {
    val showcaseShape = RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)

    Box(
        modifier = modifier
            .clip(showcaseShape)
            .background(PetPremiumColors.ShowcaseBase),
        contentAlignment = Alignment.Center
    ) {
        AnimatedPet(
            pet = pet,
            modifier = Modifier.fillMaxSize(),
            showNameOverlay = false,
            backgroundContentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PetPremiumColors.Background.copy(alpha = 0.72f),
                            PetPremiumColors.Background
                        )
                    )
                )
        )

        MedallionConnectors(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp)
                .fillMaxWidth()
                .height(128.dp)
        )

        PetMedallion(
            level = level,
            name = name,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp)
        )

        MoodPill(
            mood = mood,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 76.dp)
        )
    }
}

@Composable
private fun MedallionConnectors(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gold = PetPremiumColors.Gold
        val goldHighlight = PetPremiumColors.GoldLight
        val strokeWidth = 2.5.dp.toPx()
        val centerY = size.height / 2f
        val centerX = size.width / 2f
        val medallionRadius = MedallionSize.toPx() / 2f
        val connectorHalfWidth = 132.dp.toPx()
        val leftMedallionEdge = centerX - medallionRadius
        val rightMedallionEdge = centerX + medallionRadius
        val leftEnd = centerX - connectorHalfWidth
        val rightEnd = centerX + connectorHalfWidth
        val leftInnerEnd = leftMedallionEdge - 8.dp.toPx()
        val rightInnerEnd = rightMedallionEdge + 8.dp.toPx()

        drawLine(
            color = gold.copy(alpha = 0.42f),
            start = Offset(leftEnd, centerY),
            end = Offset(leftInnerEnd, centerY),
            strokeWidth = strokeWidth + 5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = gold.copy(alpha = 0.42f),
            start = Offset(rightEnd, centerY),
            end = Offset(rightInnerEnd, centerY),
            strokeWidth = strokeWidth + 5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = goldHighlight,
            start = Offset(leftEnd, centerY),
            end = Offset(leftInnerEnd, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = goldHighlight,
            start = Offset(rightEnd, centerY),
            end = Offset(rightInnerEnd, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PetMedallion(
    level: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(MedallionSize),
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = 14.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.medallion_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Image(
                painter = painterResource(id = R.drawable.medallion_border),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lv. $level",
                    color = PetPremiumColors.GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun LevelBadge(
    level: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(138.dp)
            .height(78.dp),
        shape = RoundedCornerShape(24.dp),
        color = PetPremiumColors.Header,
        border = BorderStroke(2.dp, PetPremiumColors.Gold),
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lv. $level",
                color = PetPremiumColors.Amber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
            Text(
                text = name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun MoodPill(
    mood: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = PetPremiumColors.MoodBackground,
        border = BorderStroke(1.dp, PetPremiumColors.MoodBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = PetPremiumColors.Green,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = mood,
                color = PetPremiumColors.Green,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PetDetailsPanel(
    pet: PetEntity,
    progressFraction: Float,
    currentLevelXp: Long,
    xpRequiredForNextLevel: Long,
    onRenameClick: () -> Unit,
    onEditCustomizationsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "$currentLevelXp / $xpRequiredForNextLevel XP",
            color = PetPremiumColors.Text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(PetPremiumColors.ProgressTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PetPremiumColors.GoldDark, PetPremiumColors.GoldLight)
                        )
                    )
            )
        }

        PetBondButton(onClick = onRenameClick)

        AttributeCard(
            pet = pet,
            onEditClick = onEditCustomizationsClick
        )

        LevelUpButton()
    }
}

@Composable
private fun PetBondButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(PetPremiumColors.Card)
            .border(1.5.dp, PetPremiumColors.Gold.copy(alpha = 0.62f), RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = PetPremiumColors.Gold,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Pet bond",
                color = PetPremiumColors.Text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AttributeCard(
    pet: PetEntity,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = PetPremiumColors.Card,
        border = BorderStroke(1.dp, PetPremiumColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attribute Card",
                    color = PetPremiumColors.Text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit customizations",
                        tint = PetPremiumColors.Gold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AttributeRow(
                icon = Icons.Default.Checkroom,
                label = "Outfit",
                value = customizationDisplayName(pet.equippedOutfit, CustomizationTypes.OUTFIT),
                iconTint = PetPremiumColors.OutfitIcon,
                valueColor = PetPremiumColors.Text
            )
            AttributeRow(
                icon = Icons.Default.Landscape,
                label = "Scene",
                value = customizationDisplayName(pet.equippedBackground, CustomizationTypes.BACKGROUND),
                iconTint = PetPremiumColors.SceneIcon,
                valueColor = PetPremiumColors.Text
            )
            AttributeRow(
                icon = Icons.Default.LocalFlorist,
                label = "Aura",
                value = customizationDisplayName(pet.equippedAura, CustomizationTypes.AURA),
                secondaryText = if (!pet.equippedAura.isNullOrBlank()) "(Equipped)" else null,
                iconTint = PetPremiumColors.AuraIcon,
                valueColor = PetPremiumColors.Text,
                secondaryColor = PetPremiumColors.Green
            )
        }
    }
}

@Composable
private fun AttributeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    valueColor: Color,
    secondaryText: String? = null,
    secondaryColor: Color = PetPremiumColors.Muted
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = label,
                color = PetPremiumColors.Muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            secondaryText?.let {
                Text(
                    text = it,
                    color = secondaryColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LevelUpButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        PetPremiumColors.ButtonDark,
                        PetPremiumColors.ButtonMid,
                        PetPremiumColors.ButtonLight
                    )
                )
            )
            .border(1.dp, PetPremiumColors.ButtonBorder, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Level Up",
            color = PetPremiumColors.ButtonText,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun customizationDisplayName(value: String?, type: String): String {
    if (value.isNullOrBlank()) return "None"

    val definitionName = EquipableConfig.definition(value)?.name
    val rawName = definitionName ?: value.replace('_', ' ')
        .split(' ')
        .joinToString(" ") { word -> word.replaceFirstChar { char -> char.titlecase() } }

    return when (type) {
        CustomizationTypes.OUTFIT -> rawName.removeSuffix(" Outfit")
        CustomizationTypes.BACKGROUND -> rawName.removeSuffix(" Background")
        else -> rawName
    }
}

@Composable
private fun GamifiedFixedHeader(
    streak: Int,
    coins: Int,
    stageName: String,
    streakCompletedToday: Boolean,
    onCoinsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PetPremiumColors.Header,
        shadowElevation = 1.dp
    ) {
        val streakTint = if (streakCompletedToday) PetPremiumColors.Amber else Color(0xFF7D7894)

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
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = streakTint,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "$streak d",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = PetPremiumColors.HeaderPill
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = PetPremiumColors.Violet,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stageName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.clickable(onClick = onCoinsClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                CoinIcon(
                    modifier = Modifier.size(20.dp),
                    tint = PetPremiumColors.Amber
                )
                Text(
                    text = "$coins",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PetPremiumColors.Violet,
                        unfocusedBorderColor = PetPremiumColors.CardBorder,
                        focusedLabelColor = PetPremiumColors.Violet,
                        unfocusedLabelColor = PetPremiumColors.Muted
                    )
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
                colors = ButtonDefaults.buttonColors(containerColor = PetPremiumColors.Violet),
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
                    Text("Cancel", color = PetPremiumColors.Muted)
                }
            }
        }
    )
}

private object PetPremiumColors {
    val Background = Color(0xFF0B1028)
    val Header = Color(0xFF0F1735)
    val HeaderPill = Color(0xFF1A2549)
    val ShowcaseBase = Color(0xFF121A36)
    val Card = Color(0xFF121A36)
    val Medallion = Color(0xFF111A38)
    val CardBorder = Color(0xFF2A355D)
    val Text = Color(0xFFF6F0FF)
    val Muted = Color(0xFFA9A4BD)
    val Gold = Color(0xFFD6A84F)
    val GoldLight = Color(0xFFFFD878)
    val GoldDark = Color(0xFF9A6A23)
    val ButtonDark = Color(0xFF9A6A23)
    val ButtonMid = Color(0xFFD6A84F)
    val ButtonLight = Color(0xFFFFE29A)
    val ButtonBorder = Color(0xFFFFE29A)
    val ButtonText = Color(0xFF2A1B05)
    val Amber = Color(0xFFFFB84D)
    val Violet = Color(0xFF8A76F9)
    val ProgressTrack = Color(0xFF2A355D)
    val Green = Color(0xFF72E69B)
    val MoodBackground = Color(0xFF123A2A)
    val MoodBorder = Color(0xFF2D6B4A)
    val OutfitIcon = Color(0xFF7DD3FC)
    val SceneIcon = Color(0xFF86EFAC)
    val AuraIcon = Color(0xFFC084FC)
}
