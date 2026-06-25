package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.LocalFlorist
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile.R
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.domain.CustomizationTypes
import com.example.mobile.domain.DragonMood
import com.example.mobile.domain.EquipableConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.domain.repository.PetRepository
import com.example.mobile.presentation.ui.components.AnimatedPet
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.GamifiedFixedHeader
import com.example.mobile.presentation.ui.components.LoadingStateCard
import com.example.mobile.presentation.ui.components.StreakCalendarOverlay
import com.example.mobile.presentation.ui.components.StreakCalendarUiState
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.DesignTokens
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    homeScreenViewModel: HomeScreenViewModel,
    onNavigateToRewardsLocked: () -> Unit,
    onNavigateToRewardsOwned: () -> Unit
) {
    val uiState by homeScreenViewModel.uiState.collectAsState()
    val isLoading by homeScreenViewModel.isLoading.collectAsState()
    val streakCalendarState by homeScreenViewModel.streakCalendarState.collectAsState()
    val error by petViewModel.error.collectAsState(initial = null)

    val pet = uiState.pet
    val currentLevelXp = ExpConfig.xpProgressInCurrentLevel(pet.xp)
    val xpRequiredForNextLevel = ExpConfig.xpRequiredForCurrentLevelProgress(pet.xp)
    val progressFraction = (currentLevelXp.toFloat() / xpRequiredForNextLevel.toFloat()).coerceIn(0f, 1f)

    PetScreenContent(
        uiState = uiState,
        isLoading = isLoading,
        error = error,
        progressFraction = progressFraction,
        currentLevelXp = currentLevelXp,
        xpRequiredForNextLevel = xpRequiredForNextLevel,
        onNavigateToRewardsLocked = onNavigateToRewardsLocked,
        onNavigateToRewardsOwned = onNavigateToRewardsOwned,
        onClearError = petViewModel::clearError,
        onConfirmRename = { newName, currentPet ->
            petViewModel.renamePet(newName.trim(), currentPet)
        },
        onStreakClick = homeScreenViewModel::openGlobalStreakCalendar,
        onStreakCalendarDismiss = homeScreenViewModel::closeStreakCalendar,
        onPreviousStreakMonth = homeScreenViewModel::showPreviousStreakMonth,
        onNextStreakMonth = homeScreenViewModel::showNextStreakMonth,
        streakCalendarState = streakCalendarState
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PetScreenContent(
    uiState: HomeScreenViewModel.UiState,
    isLoading: Boolean,
    error: String?,
    progressFraction: Float,
    currentLevelXp: Long,
    xpRequiredForNextLevel: Long,
    onNavigateToRewardsLocked: () -> Unit,
    onNavigateToRewardsOwned: () -> Unit,
    onClearError: () -> Unit,
    onConfirmRename: (String, PetEntity) -> Unit,
    onStreakClick: () -> Unit,
    onStreakCalendarDismiss: () -> Unit,
    onPreviousStreakMonth: () -> Unit,
    onNextStreakMonth: () -> Unit,
    streakCalendarState: StreakCalendarUiState?
) {
    val pet = uiState.pet
    var showRenameDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppTheme.current.background,
        topBar = {
            GamifiedFixedHeader(
                streak = uiState.globalStreak,
                coins = uiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(pet.evolutionStage),
                streakCompletedToday = uiState.globalStreakCompletedToday,
                onCoinsClick = onNavigateToRewardsLocked,
                onStreakClick = onStreakClick
            )
        }
    ) { padding ->
        if (!error.isNullOrBlank()) {
            ErrorStateCard(
                modifier = Modifier.fillMaxSize().padding(padding).padding(DesignTokens.Section.horizontalPadding),
                message = error.orEmpty(),
                onRetry = onClearError
            )
        } else if (isLoading) {
            LoadingStateCard(
                modifier = Modifier.fillMaxSize().padding(padding).padding(DesignTokens.Section.horizontalPadding),
                message = "Checking on your dragon..."
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.current.background)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PetShowcase(
                    pet = pet,
                    level = pet.level,
                    name = pet.name.ifBlank { "Baby Dragon" },
                    mood = DragonMood.from(pet.mood).displayName,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(DesignTokens.Card.padding))

                PetDetailsPanel(
                    pet = pet,
                    progressFraction = progressFraction,
                    currentLevelXp = currentLevelXp,
                    xpRequiredForNextLevel = xpRequiredForNextLevel,
                    onRenameClick = { showRenameDialog = true },
                    onEditCustomizationsClick = onNavigateToRewardsOwned
                )

                Spacer(modifier = Modifier.height(DesignTokens.space24))
            }
        }

        StreakCalendarOverlay(
            state = streakCalendarState,
            onDismiss = onStreakCalendarDismiss,
            onPreviousMonth = onPreviousStreakMonth,
            onNextMonth = onNextStreakMonth
        )
    }

    if (showRenameDialog) {
        PetRenameDialog(
            initialName = pet.name,
            onDismissRequest = { showRenameDialog = false },
            onConfirm = { newName ->
                onConfirmRename(newName, pet)
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

    Surface(
        modifier = modifier,
        shape = showcaseShape,
        color = AppTheme.current.surface,
        shadowElevation = DesignTokens.elevationSm
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
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
                                        AppTheme.current.background.copy(alpha = DesignTokens.alpha70),
                                        AppTheme.current.background
                                    )
                                )
                            )
                    )

                    MedallionConnectors(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = DesignTokens.space10)
                            .fillMaxWidth()
                            .height(MedallionSize)
                    )

                    PetMedallion(
                        level = level,
                        name = name,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = DesignTokens.space10)
                    )

                    MoodPill(
                        mood = mood,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = DesignTokens.space18, bottom = 86.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MedallionConnectors(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gold = AppTheme.current.gold
        val goldHighlight = AppTheme.current.goldDark
        val strokeWidth = 2.5.dp.toPx()
        val centerY = size.height / 2f
        val centerX = size.width / 2f
        val medallionRadius = MedallionSize.toPx() / 2f
        val connectorHalfWidth = 132.dp.toPx()
        val leftMedallionEdge = centerX - medallionRadius
        val rightMedallionEdge = centerX + medallionRadius
        val leftEnd = centerX - connectorHalfWidth
        val rightEnd = centerX + connectorHalfWidth
        val leftInnerEnd = leftMedallionEdge - DesignTokens.space8.toPx()
        val rightInnerEnd = rightMedallionEdge + DesignTokens.space8.toPx()

        drawLine(
            color = gold.copy(alpha = DesignTokens.alpha42),
            start = Offset(leftEnd, centerY),
            end = Offset(leftInnerEnd, centerY),
            strokeWidth = strokeWidth + DesignTokens.space4.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = gold.copy(alpha = DesignTokens.alpha42),
            start = Offset(rightEnd, centerY),
            end = Offset(rightInnerEnd, centerY),
            strokeWidth = strokeWidth + DesignTokens.space4.toPx(),
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
                    color = AppTheme.current.goldDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = name,
                    color = AppTheme.current.headerOnSurface,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = DesignTokens.Card.padding)
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
        shape = DesignTokens.cardCornerRounded,
        color = AppTheme.current.headerSurface,
        border = BorderStroke(DesignTokens.strokeThick, AppTheme.current.gold),
        shadowElevation = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lv. $level",
                color = AppTheme.current.amber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
            Text(
                text = name,
                color = AppTheme.current.headerOnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = DesignTokens.space12)
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
        shape = DesignTokens.cardCornerCircle,
        color = AppTheme.current.mintSurfaceActive,
        border = BorderStroke(DesignTokens.strokeThin, AppTheme.current.success)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = DesignTokens.space12, vertical = DesignTokens.space4 + 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space6)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = AppTheme.current.mint,
                modifier = Modifier.size(DesignTokens.Icon.sizeXs)
            )
            Text(
                text = mood,
                color = AppTheme.current.mint,
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
            .padding(horizontal = DesignTokens.Card.padding)
            .padding(top = DesignTokens.space8, bottom = DesignTokens.space6),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.space10)
    ) {
        Text(
            text = "$currentLevelXp/$xpRequiredForNextLevel XP",
            color = AppTheme.current.muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        PetBondButton(onClick = onRenameClick)

        AttributeCard(
            pet = pet,
            onEditClick = onEditCustomizationsClick
        )

        LevelUpButton(progressFraction = progressFraction)
    }
}

@Composable
private fun PetBondButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(DesignTokens.cardCornerCircle)
            .background(AppTheme.current.card)
            .border(DesignTokens.strokeMedium, AppTheme.current.gold.copy(alpha = DesignTokens.alpha62), DesignTokens.cardCornerCircle)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space6)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = AppTheme.current.gold,
                modifier = Modifier.size(DesignTokens.Icon.sizeXs)
            )
            Text(
                text = "Pet bond",
                color = AppTheme.current.ink,
                fontSize = 15.sp,
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
        color = AppTheme.current.card,
        border = BorderStroke(DesignTokens.strokeThin, AppTheme.current.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = DesignTokens.Card.padding, vertical = DesignTokens.space14),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.space10)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attribute Card",
                    color = AppTheme.current.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(DesignTokens.Icon.size3xl)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit customizations",
                        tint = AppTheme.current.gold,
                        modifier = Modifier.size(DesignTokens.Icon.sizeSm)
                    )
                }
            }

            AttributeRow(
                icon = Icons.Default.Checkroom,
                label = "Outfit",
                value = customizationDisplayName(pet.equippedOutfit, CustomizationTypes.OUTFIT),
                iconTint = AppTheme.current.blue,
                valueColor = AppTheme.current.ink
            )
            AttributeRow(
                icon = Icons.Default.Landscape,
                label = "Scene",
                value = customizationDisplayName(pet.equippedBackground, CustomizationTypes.BACKGROUND),
                iconTint = AppTheme.current.mint,
                valueColor = AppTheme.current.ink
            )
            AttributeRow(
                icon = Icons.Default.LocalFlorist,
                label = "Aura",
                value = customizationDisplayName(pet.equippedAura, CustomizationTypes.AURA),
                secondaryText = if (!pet.equippedAura.isNullOrBlank()) "(Equipped)" else null,
                iconTint = AppTheme.current.purple,
                valueColor = AppTheme.current.ink,
                secondaryColor = AppTheme.current.mint
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
    secondaryColor: Color = AppTheme.current.muted
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space8)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.Icon.size2xl)
                    .clip(RoundedCornerShape(DesignTokens.radiusMd))
                    .background(iconTint.copy(alpha = DesignTokens.alpha12)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(DesignTokens.Icon.sizeXs)
                )
            }
            Text(
                text = label,
                color = AppTheme.current.muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space4)
        ) {
            Text(
                text = value,
                color = valueColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            secondaryText?.let {
                Text(
                    text = it,
                    color = secondaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LevelUpButton(progressFraction: Float) {
    val clampedProgress = progressFraction.coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(DesignTokens.Button.heightSm)
            .clip(DesignTokens.cardCornerSm)
            .background(AppTheme.current.goldSoft.copy(alpha = DesignTokens.alpha25))
            .border(DesignTokens.strokeThin, AppTheme.current.goldDark, DesignTokens.cardCornerSm)
    ) {
        val progressWidth = maxWidth * clampedProgress
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(progressWidth)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppTheme.current.gold,
                            AppTheme.current.goldDark
                        )
                    )
                )
        )

        Text(
            text = "Level Up",
            color = AppTheme.current.levelUpText,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
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


private const val MAX_PET_NAME_LENGTH = 24

internal fun validatePetName(name: String): String? {
    val trimmedName = name.trim()
    return when {
        trimmedName.isEmpty() -> "Please enter a pet name"
        trimmedName.length > MAX_PET_NAME_LENGTH -> "Pet name must be $MAX_PET_NAME_LENGTH characters or fewer"
        else -> null
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun PetScreenPreview() {
    val pet = PetEntity(
        id = 1,
        name = "Luna",
        level = 3,
        xp = 180,
        evolutionStage = 1,
        equippedOutfit = "classic_blue_outfit",
        equippedBackground = "misty_meadow_background",
        equippedAura = null,
        mood = "Calm"
    )
    HabitPetTheme {
        PetScreenContent(
            uiState = HomeScreenViewModel.UiState(
                globalStreak = 4,
                habits = emptyList(),
                pet = pet,
                completedTodayXp = emptyMap(),
                totalCoins = 128,
                lastStreakDate = 0L,
                currentCombo = 0,
                lastHabitCompletionTimestamp = 0L,
                globalStreakCompletedToday = false
            ),
            isLoading = false,
            error = null,
            progressFraction = 0.48f,
            currentLevelXp = 80L,
            xpRequiredForNextLevel = 165L,
            onNavigateToRewardsLocked = {},
            onNavigateToRewardsOwned = {},
            onClearError = {},
            onConfirmRename = { _, _ -> },
            onStreakClick = {},
            onStreakCalendarDismiss = {},
            onPreviousStreakMonth = {},
            onNextStreakMonth = {},
            streakCalendarState = null
        )
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
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space8)
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
                        focusedBorderColor = AppTheme.current.violet,
                        unfocusedBorderColor = AppTheme.current.outline,
                        focusedLabelColor = AppTheme.current.violet,
                        unfocusedLabelColor = AppTheme.current.muted
                    )
                )
                helperText?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = AppTheme.current.muted)
                }
                nameError?.let {
                    Text(text = it, color = AppTheme.current.danger, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "${nameDraft.length}/$MAX_PET_NAME_LENGTH",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.current.muted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.current.violet),
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
                    Text("Cancel", color = AppTheme.current.muted)
                }
            }
        }
    )
}
