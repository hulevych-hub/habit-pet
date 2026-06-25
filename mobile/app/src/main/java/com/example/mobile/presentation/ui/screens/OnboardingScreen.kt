package com.example.mobile.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobile.ui.theme.AppTheme
import com.example.mobile.ui.theme.DesignTokens
import com.example.mobile.ui.theme.HabitPetTheme

private data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val onboardingSteps = listOf(
    OnboardingStep(
        icon = Icons.Default.Star,
        title = "Create a tiny quest",
        description = "Pick one small habit you want to nurture. It can be as simple as drinking water or reading for 5 minutes."
    ),
    OnboardingStep(
        icon = Icons.Default.CheckCircle,
        title = "Complete it to earn XP & coins",
        description = "Each completion gives you experience and coins. Build combos by completing habits close together."
    ),
    OnboardingStep(
        icon = Icons.Default.Pets,
        title = "Watch your dragon grow",
        description = "Your dragon starts as an egg. As you complete habits, it levels up, evolves through stages, and unlocks customization."
    ),
    OnboardingStep(
        icon = Icons.Default.Star,
        title = "Earn rewards & celebrate",
        description = "Hit streak milestones, open chests, unlock achievements, and dress up your dragon with outfits, backgrounds, and auras."
    )
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    OnboardingScreenContent(
        steps = onboardingSteps,
        onComplete = onComplete,
        onSkip = onSkip
    )
}

@Composable
private fun OnboardingScreenContent(
    steps: List<OnboardingStep>,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    Scaffold(
        containerColor = AppTheme.current.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = DesignTokens.Section.horizontalPadding, vertical = DesignTokens.space8)
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.current.muted
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = DesignTokens.Section.horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(DesignTokens.space24))

            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                color = AppTheme.current.ink,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Habit Pet",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = AppTheme.current.violet,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DesignTokens.space8))

            Text(
                text = "Turn tiny habits into a thriving dragon companion.",
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.current.muted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DesignTokens.space32))

            steps.forEachIndexed { index, step ->
                OnboardingStepCard(
                    stepNumber = index + 1,
                    step = step,
                    modifier = Modifier.fillMaxWidth()
                )
                if (index < steps.lastIndex) {
                    Spacer(modifier = Modifier.height(DesignTokens.space16))
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.space32))

            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignTokens.Button.heightLg),
                shape = RoundedCornerShape(DesignTokens.Button.cornerRadiusLg),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.current.violet,
                    contentColor = AppTheme.current.onPrimary
                )
            ) {
                Text(
                    text = "Create your first habit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.space12))

            Text(
                text = "You can always rename your dragon and edit everything later.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.current.muted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(DesignTokens.space24))
        }
    }
}

@Composable
private fun OnboardingStepCard(
    stepNumber: Int,
    step: OnboardingStep,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.radius4xl),
        color = AppTheme.current.card,
        shadowElevation = DesignTokens.elevationXs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.space18),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.space16)
        ) {
            Box(
                modifier = Modifier
                    .size(DesignTokens.space56)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                AppTheme.current.violet.copy(alpha = DesignTokens.alpha16),
                                AppTheme.current.primaryContainer.copy(alpha = DesignTokens.alpha16)
                            )
                        ),
                        shape = RoundedCornerShape(DesignTokens.Card.iconBackgroundRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(DesignTokens.space28)
                        .background(AppTheme.current.violet.copy(alpha = DesignTokens.alpha12), RoundedCornerShape(DesignTokens.radiusSm)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.violet
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.space4)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.space8)
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = AppTheme.current.violet,
                        modifier = Modifier.size(DesignTokens.Icon.sizeMd)
                    )
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AppTheme.current.ink
                    )
                }
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.current.muted
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=390px,height=844px,dpi=420")
@Composable
private fun OnboardingScreenPreview() {
    HabitPetTheme {
        OnboardingScreenContent(
            steps = onboardingSteps,
            onComplete = {},
            onSkip = {}
        )
    }
}
