package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.R
import com.example.mobile.data.local.entities.AchievementEntity
import com.example.mobile.data.local.entities.PetEntity
import com.example.mobile.data.local.entities.StatisticsEntity
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.domain.ExpConfig
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.ui.components.GamifiedFixedHeader
import com.example.mobile.presentation.ui.reward.AnimatedRewardChest
import com.example.mobile.presentation.viewmodel.AchievementViewModel
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.AppThemeColors
import com.example.mobile.ui.theme.AutumnColors
import com.example.mobile.ui.theme.AppThemeOption
import com.example.mobile.ui.theme.AppThemePrefs
import com.example.mobile.ui.theme.DesignTokens
import com.example.mobile.ui.theme.HabitPetTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AchievementScreen(
    achievementViewModel: AchievementViewModel = hiltViewModel(),
    homeScreenViewModel: HomeScreenViewModel? = null
) {
    val achievements by achievementViewModel.achievements.collectAsState(initial = emptyList())
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val error by achievementViewModel.error.collectAsState()
    val claimableCount by achievementViewModel.claimableAchievementCount.collectAsState()
    val isClaiming by achievementViewModel.isClaiming.collectAsState()
    val stats by achievementViewModel.statistics.collectAsState()
    val petState by achievementViewModel.pet.collectAsState()
    val ownedCustomizations by achievementViewModel.ownedCustomizations.collectAsState()
    val currentHabitCount by achievementViewModel.habitCount.collectAsState()
    val palette = AchievementHallPalette.current()

    AchievementScreenContent(
        achievements = achievements,
        isLoading = isLoading,
        error = error,
        claimableCount = claimableCount,
        isClaiming = isClaiming,
        rewardLabels = achievementViewModel::rewardLabels,
        progressFor = achievementViewModel::progressFor,
        progressLabel = achievementViewModel::progressLabel,
        stats = stats,
        petState = petState,
        ownedCustomizations = ownedCustomizations,
        currentHabitCount = currentHabitCount,
        palette = palette,
        onRetry = achievementViewModel::retryLoadAchievements,
        onClaim = { achievementId -> achievementViewModel.claimAchievement(achievementId) },
        onClaimAll = achievementViewModel::claimAllAchievements,
        homeScreenViewModel = homeScreenViewModel
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AchievementScreenContent(
    achievements: List<AchievementEntity>,
    isLoading: Boolean,
    error: String?,
    claimableCount: Int,
    isClaiming: Boolean,
    rewardLabels: (AchievementEntity) -> List<String>,
    progressFor: (AchievementEntity, StatisticsEntity, PetEntity, Int, Int) -> Int,
    progressLabel: (Int, AchievementEntity) -> String,
    stats: StatisticsEntity,
    petState: PetEntity,
    ownedCustomizations: Int,
    currentHabitCount: Int,
    palette: AchievementHallPalette,
    onRetry: () -> Unit,
    onClaim: (String) -> Unit,
    onClaimAll: () -> Unit,
    homeScreenViewModel: HomeScreenViewModel? = null
) {
    val headerUiState by (homeScreenViewModel?.uiState?.collectAsState()
        ?: return)
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GamifiedFixedHeader(
                streak = headerUiState.globalStreak,
                coins = headerUiState.totalCoins,
                stageName = ExpConfig.evolutionStageName(petState.evolutionStage),
                streakCompletedToday = headerUiState.globalStreakCompletedToday,
                streakPartialToday = headerUiState.globalStreakPartialToday,
                onCoinsClick = {},
                onStreakClick = if (homeScreenViewModel != null) {
                    { homeScreenViewModel.openGlobalStreakCalendar() }
                } else {
                    {}
                }
            )
        },
        bottomBar = {
            if (claimableCount > 1) {
                ClaimAllRewardsBar(
                    claimableCount = claimableCount,
                    isClaiming = isClaiming,
                    onClick = onClaimAll,
                    palette = palette
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = palette.backgroundGradient))
        ) {
            ThemeHallOverlay(
                palette = palette,
                modifier = Modifier.matchParentSize()
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    top = DesignTokens.space10,
                    start = DesignTokens.space14,
                    end = DesignTokens.space14,
                    bottom = DesignTokens.Card.padding
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space12)
            ) {
                item {
                    GrandMilestoneHeader(palette = palette)
                }

                when {
                    isLoading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.space32),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = palette.gold)
                        }
                    }
                    !error.isNullOrBlank() -> item {
                        ErrorStateCard(
                            message = error.orEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            onRetry = onRetry
                        )
                    }
                    achievements.isEmpty() -> item {
                        EmptyStateCard(
                            title = "Your first milestone is close",
                            message = "Finish a habit, earn XP, or grow your streak to awaken the first achievement.",
                            hint = "Every small win moves your dragon closer to its first badge.",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(
                    items = achievements,
                    key = { achievement -> achievement.id }
                ) { achievement ->
                    val definition = AchievementsConfig.achievementById(achievement.id)
                    val target = definition?.targetValue?.coerceAtLeast(1) ?: 1
                    val progress = progressFor(
                        achievement,
                        stats,
                        petState,
                        ownedCustomizations,
                        currentHabitCount
                    )
                    AchievementHallCard(
                        achievement = achievement,
                        title = definition?.name ?: achievement.id,
                        description = definition?.description ?: "Keep growing.",
                        progressFraction = (progress.toFloat() / target.toFloat()).coerceIn(0f, 1f),
                        progressLabel = progressLabel(progress, achievement),
                        rewards = rewardLabels(achievement),
                        onClaim = { onClaim(achievement.id) },
                        palette = palette
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeHallOverlay(
    palette: AchievementHallPalette,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val line = DesignTokens.strokeThin.toPx()
        val lightLineAlpha = if (palette.isAutumn) DesignTokens.alpha4 else DesignTokens.alpha8

        repeat(18) { index ->
            val y = (index + 1) * height / 19f
            drawLine(
                color = palette.overlayLineDark.copy(alpha = DesignTokens.alpha24),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = line
            )
        }

        repeat(7) { index ->
            val x = (index + 1) * width / 8f
            drawLine(
                color = palette.overlayLineLight.copy(alpha = lightLineAlpha),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = line
            )
        }
    }
}

@Composable
private fun GrandMilestoneHeader(
    palette: AchievementHallPalette,
    modifier: Modifier = Modifier
) {
    val plaqueShape = DesignTokens.cardCorner
    val headerBorder = Brush.linearGradient(colors = palette.headerBorder)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .border(width = DesignTokens.strokeThick, brush = headerBorder, shape = plaqueShape),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = plaqueShape,
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.elevationXl)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(colors = palette.headerGradient),
                    shape = plaqueShape
                )
                .padding(DesignTokens.Card.padding)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Grand",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.headerInk
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Milestone Hall",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.headerInk
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(
                            width = DesignTokens.strokeThin,
                            color = palette.trophyBorder.copy(alpha = if (palette.isAutumn) DesignTokens.alpha62 else DesignTokens.alpha42),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_trophy_gold),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementHallCard(
    achievement: com.example.mobile.data.local.entities.AchievementEntity,
    title: String,
    description: String,
    progressFraction: Float,
    progressLabel: String,
    rewards: List<String>,
    onClaim: () -> Unit,
    palette: AchievementHallPalette
) {
    val isClaimed = achievement.isClaimed
    val isUnlocked = achievement.isUnlocked
    val isClaimable = isUnlocked && !isClaimed && progressFraction >= 0.999f
    val cardShape = DesignTokens.cardCorner
    val borderBrush = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> Brush.linearGradient(colors = palette.crownBorder)
        isClaimed -> Brush.linearGradient(colors = palette.claimedBorder)
        isClaimable -> Brush.linearGradient(colors = palette.claimableBorder)
        isUnlocked -> Brush.linearGradient(colors = palette.unlockedBorder)
        else -> Brush.linearGradient(colors = palette.lockedBorder)
    }
    val backgroundBrush = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> Brush.linearGradient(colors = palette.crownBackground)
        isClaimed -> Brush.linearGradient(colors = palette.claimedBackground)
        isClaimable -> Brush.linearGradient(colors = palette.claimableBackground)
        isUnlocked -> Brush.linearGradient(colors = palette.unlockedBackground)
        else -> Brush.linearGradient(colors = palette.lockedBackground)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClaimable, onClick = onClaim)
            .border(width = DesignTokens.strokeThick, brush = borderBrush, shape = cardShape),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isClaimable) DesignTokens.elevationLg else DesignTokens.elevationMd)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush, cardShape)
                .padding(DesignTokens.space14)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space10)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.space12),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HallIconBox(
                        achievement = achievement,
                        isClaimed = isClaimed,
                        palette = palette
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.space2)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isClaimed) palette.claimedInk else palette.cardInk
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isClaimed) palette.claimedMuted else palette.cardMuted
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isClaimed) {
                        Surface(
                            shape = DesignTokens.cardCornerCircle,
                            color = if (achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION) {
                                palette.crownBadgeSurface
                            } else {
                                palette.claimedBadgeSurface
                            }
                        ) {
                            Text(
                                text = "Claimed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION) {
                                    palette.crownBadgeText
                                } else {
                                    palette.claimedBadgeText
                                },
                                modifier = Modifier.padding(horizontal = DesignTokens.space10, vertical = DesignTokens.space4)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isClaimed) palette.claimedMuted else palette.cardMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = rewards.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isClaimed) palette.claimedAccent else palette.cardInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isClaimed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(DesignTokens.space8)
                            .background(palette.progressTrack.copy(alpha = palette.progressTrackAlpha), DesignTokens.cardCornerCircle)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                .height(DesignTokens.space8)
                                .background(
                                    brush = if (isClaimable) {
                                        Brush.linearGradient(colors = palette.claimableProgress)
                                    } else {
                                        Brush.linearGradient(colors = palette.unlockedProgress)
                                    },
                                    DesignTokens.cardCornerCircle
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HallIconBox(
    achievement: com.example.mobile.data.local.entities.AchievementEntity,
    isClaimed: Boolean,
    palette: AchievementHallPalette
) {
    val iconTint = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> palette.crownIconTint
        isClaimed -> palette.claimedIconTint
        achievement.isUnlocked -> palette.unlockedIconTint
        else -> palette.lockedIconTint
    }
    val iconBackground = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> palette.crownIconBackground
        isClaimed -> palette.claimedIconBackground
        achievement.id == AchievementsConfig.FIRST_AURA_GLOW -> if (palette.isAutumn) Color.Transparent else palette.auraIconBackground
        achievement.isUnlocked -> palette.unlockedIconBackground
        else -> palette.lockedIconBackground
    }

    Box(
        modifier = Modifier
            .size(DesignTokens.Card.iconSizeLg)
            .background(iconBackground, RoundedCornerShape(DesignTokens.radius2xl)),
        contentAlignment = Alignment.Center
    ) {
        when {
            isClaimed && achievement.id == AchievementsConfig.FIRST_HABIT -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(34.dp)
                )
            }
            achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> {
                Image(
                    painter = painterResource(R.drawable.ic_crown_gold),
                    contentDescription = null,
                    modifier = Modifier.size(38.dp)
                )
            }
            achievement.id == AchievementsConfig.FIRST_AURA_GLOW -> {
                Image(
                    painter = painterResource(R.drawable.ic_sakura_aura),
                    contentDescription = null,
                    modifier = Modifier.size(46.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun ClaimAllRewardsBar(
    claimableCount: Int,
    isClaiming: Boolean,
    onClick: () -> Unit,
    palette: AchievementHallPalette,
    modifier: Modifier = Modifier
) {
    val enabled = claimableCount > 0 && !isClaiming
    val buttonShape = DesignTokens.cardCornerRounded
    val enabledBrush = Brush.linearGradient(colors = palette.buttonGradient)
    val disabledBrush = Brush.linearGradient(colors = palette.disabledButton)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .padding(horizontal = DesignTokens.Card.padding, vertical = DesignTokens.space8),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clickable(enabled = enabled, onClick = onClick)
                .clip(buttonShape)
                .border(width = DesignTokens.strokeThick, brush = if (enabled) enabledBrush else disabledBrush, shape = buttonShape)
                .background(if (enabled) enabledBrush else disabledBrush, buttonShape)
                .padding(horizontal = DesignTokens.Button.paddingHorizontal),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoinSparkle(left = true)
                Spacer(modifier = Modifier.width(DesignTokens.space10))
                Text(
                    text = if (isClaiming) "Claiming Rewards..." else "Claim All Rewards",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) palette.buttonText else palette.disabledText
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(DesignTokens.space14))
                AnimatedRewardChest(
                    size = DesignTokens.Icon.touchTarget,
                    tint = palette.buttonText,
                    modifier = Modifier.size(DesignTokens.Icon.touchTarget)
                )
                Spacer(modifier = Modifier.width(DesignTokens.space10))
                CoinSparkle(left = false)
            }
        }
    }
}

@Composable
private fun CoinSparkle(left: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-4).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val coins = if (left) listOf(0.9f, 1f, 0.82f) else listOf(0.82f, 1f, 0.9f)
        coins.forEachIndexed { index, alpha ->
            Icon(
                painter = painterResource(R.drawable.ic_coin),
                contentDescription = null,
                tint = Color.White.copy(alpha = alpha),
                modifier = Modifier.size(if (index == 1) DesignTokens.Icon.sizeMd else DesignTokens.Icon.sizeXs)
            )
        }
    }
}

private data class AchievementHallPalette(
    val id: String,
    val isAutumn: Boolean,
    val gold: Color,
    val backgroundGradient: List<Color>,
    val overlayLineDark: Color,
    val overlayLineLight: Color,
    val headerBorder: List<Color>,
    val headerGradient: List<Color>,
    val headerInk: Color,
    val trophyBorder: Color,
    val crownBorder: List<Color>,
    val crownBackground: List<Color>,
    val crownIconTint: Color,
    val crownIconBackground: Color,
    val crownBadgeSurface: Color,
    val crownBadgeText: Color,
    val claimedBorder: List<Color>,
    val claimedBackground: List<Color>,
    val claimedIconTint: Color,
    val claimedIconBackground: Color,
    val claimedBadgeSurface: Color,
    val claimedBadgeText: Color,
    val claimedInk: Color,
    val claimedMuted: Color,
    val claimedAccent: Color,
    val claimableBorder: List<Color>,
    val claimableBackground: List<Color>,
    val claimableProgress: List<Color>,
    val unlockedBorder: List<Color>,
    val unlockedBackground: List<Color>,
    val unlockedProgress: List<Color>,
    val lockedBorder: List<Color>,
    val lockedBackground: List<Color>,
    val lockedIconTint: Color,
    val lockedIconBackground: Color,
    val unlockedIconTint: Color,
    val unlockedIconBackground: Color,
    val auraIconBackground: Color,
    val cardInk: Color,
    val cardMuted: Color,
    val progressTrack: Color,
    val progressTrackAlpha: Float,
    val disabledButton: List<Color>,
    val buttonText: Color,
    val disabledText: Color,
    val buttonGradient: List<Color>
) {
    companion object {
        fun current(): AchievementHallPalette =
            if (AppThemePrefs.currentTheme() == AppThemeOption.AUTUMN) autumn else fromTheme(AppTheme.current)

        val autumn: AchievementHallPalette = AchievementHallPalette(
            id = "autumn",
            isAutumn = true,
            gold = AutumnColors.gold,
            backgroundGradient = listOf(AutumnColors.stoneBase, AutumnColors.stoneBase),
            overlayLineDark = AutumnColors.stoneLineDark,
            overlayLineLight = AutumnColors.stoneLineLight,
            headerBorder = listOf(AutumnColors.goldLight, AutumnColors.gold, AutumnColors.goldDark),
            headerGradient = listOf(AutumnColors.headerGoldTop, AutumnColors.headerGoldMiddle, AutumnColors.headerGoldBottom),
            headerInk = AutumnColors.deepInk,
            trophyBorder = Color(0xFFFFF3C4),
            crownBorder = listOf(AutumnColors.goldLight, AutumnColors.gold, AutumnColors.goldDark),
            crownBackground = listOf(Color(0xFFFFE7A3), Color(0xFFF4C76B), Color(0xFFD99A31)),
            crownIconTint = AutumnColors.goldDark,
            crownIconBackground = Color(0xFFFFE7A3),
            crownBadgeSurface = Color(0xFF6B3F0C).copy(alpha = DesignTokens.alpha24),
            crownBadgeText = Color(0xFF7A4A12),
            claimedBorder = listOf(Color(0xFFE8EEF2), Color(0xFFB9C2C8), Color(0xFF8F9AA3)),
            claimedBackground = listOf(Color(0xFFF7FAFC), Color(0xFFDDE5EA), Color(0xFFB8C4CC)),
            claimedIconTint = Color(0xFF1F6B45),
            claimedIconBackground = Color(0xFFE8F3EC),
            claimedBadgeSurface = Color(0xFF24332A).copy(alpha = DesignTokens.alpha16),
            claimedBadgeText = Color(0xFF1F6B45),
            claimedInk = AutumnColors.claimedInk,
            claimedMuted = AutumnColors.claimedMuted,
            claimedAccent = AutumnColors.goldDark,
            claimableBorder = listOf(AutumnColors.goldLight, Color(0xFFF472B6), AutumnColors.auraPurple),
            claimableBackground = listOf(Color(0xFFFFE4F3), Color(0xFFF472B6), Color(0xFF7C3AED)),
            claimableProgress = listOf(AutumnColors.goldLight, Color(0xFFF472B6), AutumnColors.auraPurple),
            unlockedBorder = listOf(AutumnColors.gold, AutumnColors.amber),
            unlockedBackground = listOf(Color(0xFFFFF2CC), Color(0xFFF6C85F), Color(0xFFD69A2D)),
            unlockedProgress = listOf(AutumnColors.gold, AutumnColors.amber),
            lockedBorder = listOf(Color(0xFF4A4240), Color(0xFF2E2928)),
            lockedBackground = listOf(Color(0xFF4A4240), Color(0xFF2E2928)),
            lockedIconTint = Color.White.copy(alpha = DesignTokens.alpha55),
            lockedIconBackground = Color(0xFF2B2422).copy(alpha = DesignTokens.alpha34),
            unlockedIconTint = Color.White,
            unlockedIconBackground = Color(0xFF7C3AED).copy(alpha = DesignTokens.alpha32),
            auraIconBackground = Color.Transparent,
            cardInk = Color.White,
            cardMuted = Color(0xFFFFF7ED).copy(alpha = DesignTokens.alpha86),
            progressTrack = Color(0xFF2B2422),
            progressTrackAlpha = DesignTokens.alpha22,
            disabledButton = listOf(Color(0xFF5A514B), Color(0xFF3A3431)),
            buttonText = AutumnColors.deepInk,
            disabledText = AutumnColors.disabledText,
            buttonGradient = listOf(AutumnColors.goldLight, AutumnColors.gold, AutumnColors.goldDark)
        )

        private fun fromTheme(colors: AppThemeColors): AchievementHallPalette {
            val isDarkTheme = AppThemePrefs.currentTheme().isDark
            val claimedInk = if (isDarkTheme) colors.headerOnSurface else colors.ink
            val claimedMuted = if (isDarkTheme) colors.mutedStrong else colors.softInk
            val lockedIconBackground = colors.outline.copy(alpha = if (isDarkTheme) DesignTokens.alpha28 else DesignTokens.alpha22)
            val progressTrackAlpha = if (isDarkTheme) DesignTokens.alpha55 else DesignTokens.alpha75

            return AchievementHallPalette(
                id = AppThemePrefs.currentTheme().id,
                isAutumn = false,
                gold = colors.gold,
                backgroundGradient = listOf(colors.rewardBackdropStart, colors.rewardBackdropCenter, colors.rewardBackdropEnd),
                overlayLineDark = if (isDarkTheme) colors.primaryContainer else colors.primary,
                overlayLineLight = colors.gold,
                headerBorder = listOf(colors.primary, colors.gold, colors.secondary),
                headerGradient = listOf(colors.headerGradientStart, colors.primaryContainer, colors.headerGradientEnd),
                headerInk = colors.headerOnSurface,
                trophyBorder = colors.gold,
                crownBorder = listOf(colors.goldSoft, colors.gold, colors.goldDark),
                crownBackground = listOf(colors.goldSoft, colors.gold, colors.amberDark),
                crownIconTint = colors.goldDark,
                crownIconBackground = colors.goldSoft,
                crownBadgeSurface = colors.amber.copy(alpha = DesignTokens.alpha20),
                crownBadgeText = colors.goldDark,
                claimedBorder = if (isDarkTheme) {
                    listOf(colors.primaryContainer, colors.lavenderSoft, colors.amethystSoft)
                } else {
                    listOf(colors.surface, colors.goldSoft, colors.lavenderSoft)
                },
                claimedBackground = if (isDarkTheme) {
                    listOf(colors.primaryContainer, colors.lavenderSoft, colors.amethystSoft)
                } else {
                    listOf(colors.surface, colors.goldSoft, colors.lavenderSoft)
                },
                claimedIconTint = colors.success,
                claimedIconBackground = colors.successSoft,
                claimedBadgeSurface = colors.success.copy(alpha = DesignTokens.alpha16),
                claimedBadgeText = colors.success,
                claimedInk = claimedInk,
                claimedMuted = claimedMuted,
                claimedAccent = colors.goldDark,
                claimableBorder = listOf(colors.goldSoft, colors.pink, colors.primary),
                claimableBackground = listOf(colors.pinkSoft, colors.pink, colors.primary),
                claimableProgress = listOf(colors.goldSoft, colors.pink, colors.primary),
                unlockedBorder = listOf(colors.gold, colors.amber),
                unlockedBackground = listOf(colors.secondaryContainer, colors.gold, colors.amber),
                unlockedProgress = listOf(colors.gold, colors.amber),
                lockedBorder = if (isDarkTheme) listOf(colors.background, colors.surfaceVariant) else listOf(colors.outline, colors.surfaceVariant),
                lockedBackground = listOf(colors.background, colors.surfaceVariant),
                lockedIconTint = colors.inactiveIcon,
                lockedIconBackground = lockedIconBackground,
                unlockedIconTint = if (isDarkTheme) Color.White else colors.primary,
                unlockedIconBackground = colors.primary.copy(alpha = DesignTokens.alpha32),
                auraIconBackground = colors.primary.copy(alpha = DesignTokens.alpha20),
                cardInk = if (isDarkTheme) Color.White else colors.ink,
                cardMuted = if (isDarkTheme) Color.White.copy(alpha = DesignTokens.alpha86) else colors.softInk,
                progressTrack = colors.progressTrack,
                progressTrackAlpha = progressTrackAlpha,
                disabledButton = if (isDarkTheme) listOf(colors.inactiveIcon, colors.violetMuted) else listOf(colors.outline, colors.purpleSoft),
                buttonText = colors.onSecondary,
                disabledText = colors.muted,
                buttonGradient = listOf(colors.goldSoft, colors.gold, colors.goldDark)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun AchievementScreenPreview() {
    HabitPetTheme {
        AchievementScreenContent(
            achievements = listOf(
                AchievementEntity(
                    id = AchievementsConfig.FIRST_HABIT,
                    progress = 3,
                    isUnlocked = true,
                    isClaimed = false
                ),
                AchievementEntity(
                    id = AchievementsConfig.FIRST_CUSTOMIZATION,
                    progress = 1,
                    isUnlocked = true,
                    isClaimed = true
                ),
                AchievementEntity(
                    id = AchievementsConfig.FIRST_AURA_GLOW,
                    progress = 0,
                    isUnlocked = false,
                    isClaimed = false
                )
            ),
            isLoading = false,
            error = null,
            claimableCount = 1,
            isClaiming = false,
            rewardLabels = { listOf("+50 coins") },
            progressFor = { achievement, _, _, _, _ -> achievement.progress },
            progressLabel = { progress, achievement -> "$progress/${AchievementsConfig.achievementById(achievement.id)?.targetValue ?: 1}" },
            stats = com.example.mobile.data.local.entities.StatisticsEntity(id = 1),
            petState = PetEntity(id = 1),
            ownedCustomizations = 1,
            currentHabitCount = 3,
            palette = AchievementHallPalette.current(),
            onRetry = {},
            onClaim = {},
            onClaimAll = {}
        )
    }
}
