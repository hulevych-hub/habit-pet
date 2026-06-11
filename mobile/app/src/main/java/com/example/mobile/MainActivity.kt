package com.example.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.mobile.domain.ActivityTimelineEngine
import com.example.mobile.domain.DragonMoodEngine
import com.example.mobile.domain.repository.StatisticsRepository
import com.example.mobile.navigation.HabitPetNavGraph
import com.example.mobile.presentation.ui.feedback.MicroFeedbackManager
import com.example.mobile.presentation.ui.feedback.MicroFeedbackOverlay
import com.example.mobile.presentation.ui.reward.RewardManager
import com.example.mobile.presentation.ui.reward.RewardOverlayHost
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.AndroidEntryPoint
import com.example.mobile.util.NotificationPrefs
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var rewardManager: RewardManager

    @Inject
    lateinit var activityTimelineEngine: ActivityTimelineEngine

    @Inject
    lateinit var microFeedbackManager: MicroFeedbackManager

    @Inject
    lateinit var dragonMoodEngine: DragonMoodEngine

    @Inject
    lateinit var statisticsRepository: StatisticsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val statistics = statisticsRepository.getStatistics().firstOrNull()
            NotificationPrefs.recordStreak(this@MainActivity, statistics?.currentStreak ?: 0)
            NotificationPrefs.recordLastActiveSession(this@MainActivity)

            activityTimelineEngine.start()
            dragonMoodEngine.refreshMood()
        }

        // ✅ Proper edge-to-edge setup (modern Android way)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            HabitPetTheme {

                androidx.compose.foundation.layout.Box {

                    HabitPetNavGraph(
                        microFeedbackManager = microFeedbackManager
                    )

                    MicroFeedbackOverlay(
                        manager = microFeedbackManager
                    )

                    RewardOverlayHost(
                        rewardManager = rewardManager,
                        onRewardCompleted = {
                            rewardManager.rewardCompleted()
                        }
                    )
                }
            }
        }

        // ✅ Hide system bars during reward cinematic
        lifecycleScope.launch {
            rewardManager.isDisplayingReward.collect { isDisplaying ->

                val controller =
                    WindowCompat.getInsetsController(window, window.decorView)

                if (isDisplaying) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    override fun onBackPressed() {
        // Prevent navigation during reward cinematic
        if (rewardManager.isDisplayingReward.value) {
            return
        }
        super.onBackPressed()
    }
}