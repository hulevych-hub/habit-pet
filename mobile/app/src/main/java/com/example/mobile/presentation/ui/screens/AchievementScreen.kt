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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile.R
import com.example.mobile.domain.AchievementsConfig
import com.example.mobile.presentation.ui.components.EmptyStateCard
import com.example.mobile.presentation.ui.components.ErrorStateCard
import com.example.mobile.presentation.viewmodel.AchievementViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AchievementScreen(
    achievementViewModel: AchievementViewModel = hiltViewModel()
) {
    val achievements by achievementViewModel.achievements.collectAsState(initial = emptyList())
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val error by achievementViewModel.error.collectAsState()
    val claimableCount by achievementViewModel.claimableAchievementCount.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            ClaimAllRewardsBar(
                claimableCount = claimableCount,
                onClick = achievementViewModel::claimAllAchievements
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MilestoneHallColors.stoneBase)
        ) {
            StoneWallOverlay(modifier = Modifier.matchParentSize())

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    top = 10.dp,
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    GrandMilestoneHeader()
                }

                when {
                    isLoading -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MilestoneHallColors.gold)
                        }
                    }
                    !error.isNullOrBlank() -> item {
                        ErrorStateCard(
                            message = error.orEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            onRetry = achievementViewModel::retryLoadAchievements
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
                    AchievementHallCard(
                        achievement = achievement,
                        title = AchievementsConfig.achievementById(achievement.id)?.name ?: achievement.id,
                        description = AchievementsConfig.achievementById(achievement.id)?.description ?: "Keep growing.",
                        progressFraction = achievementViewModel.progressFraction(
                            achievementViewModel.progressFor(
                                achievement = achievement,
                                stats = achievementViewModel.statistics.value,
                                petState = achievementViewModel.pet.value,
                                ownedCustomizationCount = achievementViewModel.ownedCustomizations.value,
                                currentHabitCount = achievementViewModel.habitCount.value
                            ),
                            achievement
                        ),
                        progressLabel = achievementViewModel.progressLabel(
                            achievementViewModel.progressFor(
                                achievement = achievement,
                                stats = achievementViewModel.statistics.value,
                                petState = achievementViewModel.pet.value,
                                ownedCustomizationCount = achievementViewModel.ownedCustomizations.value,
                                currentHabitCount = achievementViewModel.habitCount.value
                            ),
                            achievement
                        ),
                        rewards = achievementViewModel.rewardLabels(achievement),
                        onClaim = { achievementViewModel.claimAchievement(achievement.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StoneWallOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val line = 1.dp.toPx()

        repeat(18) { index ->
            val y = (index + 1) * height / 19f
            drawLine(
                color = Color(0xFF2F2A2A).copy(alpha = 0.24f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = line
            )
        }

        repeat(7) { index ->
            val x = (index + 1) * width / 8f
            drawLine(
                color = Color(0xFFFFF1C7).copy(alpha = 0.045f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = line
            )
        }
    }
}

@Composable
private fun GrandMilestoneHeader(
    modifier: Modifier = Modifier
) {
    val plaqueShape = RoundedCornerShape(22.dp)
    val goldBorder = Brush.linearGradient(
        colors = listOf(MilestoneHallColors.goldLight, MilestoneHallColors.gold, MilestoneHallColors.goldDark)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(108.dp)
            .border(width = 2.dp, brush = goldBorder, shape = plaqueShape),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = plaqueShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MilestoneHallColors.headerGoldTop,
                            MilestoneHallColors.headerGoldMiddle,
                            MilestoneHallColors.headerGoldBottom
                        )
                    ),
                    shape = plaqueShape
                )
                .padding(16.dp)
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
                            color = MilestoneHallColors.deepInk
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Milestone Hall",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MilestoneHallColors.deepInk
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFFF3C4).copy(alpha = 0.62f),
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
    onClaim: () -> Unit
) {
    val isClaimed = achievement.isClaimed
    val isUnlocked = achievement.isUnlocked
    val isClaimable = isUnlocked && !isClaimed && progressFraction >= 0.999f
    val cardShape = RoundedCornerShape(20.dp)
    val borderBrush = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> Brush.linearGradient(
            colors = listOf(MilestoneHallColors.goldLight, MilestoneHallColors.gold, MilestoneHallColors.goldDark)
        )
        isClaimed -> Brush.linearGradient(
            colors = listOf(Color(0xFFE8EEF2), Color(0xFFB9C2C8), Color(0xFF8F9AA3))
        )
        isClaimable -> Brush.linearGradient(
            colors = listOf(MilestoneHallColors.goldLight, Color(0xFFE879F9), MilestoneHallColors.auraPurple)
        )
        isUnlocked -> Brush.linearGradient(
            colors = listOf(MilestoneHallColors.gold, MilestoneHallColors.amber)
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF4A4240), Color(0xFF2E2928))
        )
    }
    val backgroundBrush = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> Brush.linearGradient(
            colors = listOf(Color(0xFFFFE7A3), Color(0xFFF4C76B), Color(0xFFD99A31))
        )
        isClaimed -> Brush.linearGradient(
            colors = listOf(Color(0xFFF7FAFC), Color(0xFFDDE5EA), Color(0xFFB8C4CC))
        )
        isClaimable -> Brush.linearGradient(
            colors = listOf(Color(0xFFFFE4F3), Color(0xFFF472B6), Color(0xFF7C3AED))
        )
        isUnlocked -> Brush.linearGradient(
            colors = listOf(Color(0xFFFFF2CC), Color(0xFFF6C85F), Color(0xFFD69A2D))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF5A514B), Color(0xFF3F3936))
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClaimable, onClick = onClaim)
            .border(width = 2.dp, brush = borderBrush, shape = cardShape),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isClaimable) 5.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush, cardShape)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HallIconBox(achievement = achievement, isClaimed = isClaimed)

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isClaimed) MilestoneHallColors.claimedInk else Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isClaimed) MilestoneHallColors.claimedMuted else Color(0xFFFFF7ED).copy(alpha = 0.86f)
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (isClaimed) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION) {
                                Color(0xFF6B3F0C).copy(alpha = 0.24f)
                            } else {
                                Color(0xFF24332A).copy(alpha = 0.16f)
                            }
                        ) {
                            Text(
                                text = "Claimed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION) {
                                    Color(0xFF7A4A12)
                                } else {
                                    Color(0xFF1F6B45)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                        color = if (isClaimed) MilestoneHallColors.claimedMuted else Color(0xFFFFF7ED).copy(alpha = 0.86f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = rewards.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isClaimed) MilestoneHallColors.goldDark else Color(0xFFFFF7ED),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isClaimed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFF2B2422).copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                .height(8.dp)
                                .background(
                                    brush = if (isClaimable) {
                                        Brush.linearGradient(
                                            colors = listOf(MilestoneHallColors.goldLight, Color(0xFFF472B6), MilestoneHallColors.auraPurple)
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(MilestoneHallColors.gold, MilestoneHallColors.amber)
                                        )
                                    },
                                    RoundedCornerShape(999.dp)
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
    isClaimed: Boolean
) {
    val iconTint = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> MilestoneHallColors.goldDark
        isClaimed -> Color(0xFF1F6B45)
        achievement.isUnlocked -> Color.White
        else -> Color.White.copy(alpha = 0.55f)
    }
    val iconBackground = when {
        isClaimed && achievement.id == AchievementsConfig.FIRST_CUSTOMIZATION -> Color(0xFFFFE7A3)
        isClaimed -> Color(0xFFE8F3EC)
        achievement.id == AchievementsConfig.FIRST_AURA_GLOW -> Color.Transparent
        achievement.isUnlocked -> Color(0xFF7C3AED).copy(alpha = 0.32f)
        else -> Color(0xFF2B2422).copy(alpha = 0.34f)
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .background(iconBackground, RoundedCornerShape(18.dp)),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = claimableCount > 0
    val buttonShape = RoundedCornerShape(24.dp)
    val goldBrush = Brush.linearGradient(
        colors = listOf(MilestoneHallColors.goldLight, MilestoneHallColors.gold, MilestoneHallColors.goldDark)
    )
    val disabledBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF5A514B), Color(0xFF3A3431))
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .clickable(enabled = enabled, onClick = onClick)
                .clip(buttonShape)
                .border(width = 2.dp, brush = if (enabled) goldBrush else disabledBrush, shape = buttonShape)
                .background(if (enabled) goldBrush else disabledBrush, buttonShape)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoinSparkle(left = true)
                Spacer(modifier = Modifier.width(10.dp))
                Image(
                    painter = painterResource(R.drawable.chest_closed),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Claim All Rewards",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (enabled) MilestoneHallColors.deepInk else Color(0xFFD8C7A8)
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(10.dp))
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
                modifier = Modifier.size(if (index == 1) 20.dp else 16.dp)
            )
        }
    }
}

private object MilestoneHallColors {
    val stoneBase = Color(0xFF4A423D)
    val headerGoldTop = Color(0xFFFFE6A0)
    val headerGoldMiddle = Color(0xFFF2B94A)
    val headerGoldBottom = Color(0xFFC98224)
    val goldLight = Color(0xFFFFF1A8)
    val gold = Color(0xFFFFD45A)
    val goldDark = Color(0xFF9A620E)
    val amber = Color(0xFFFFB84D)
    val auraPurple = Color(0xFF7C3AED)
    val deepInk = Color(0xFF2A1B08)
    val claimedInk = Color(0xFF26332A)
    val claimedMuted = Color(0xFF5D6B62)
}
