package com.example.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import com.example.mobile.navigation.HabitPetNavGraph
import com.example.mobile.presentation.ui.reward.RewardManager
import com.example.mobile.presentation.ui.reward.RewardOverlay
import com.example.mobile.ui.theme.HabitPetTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var rewardManager: RewardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HabitPetTheme {

                Box {

                    HabitPetNavGraph()

                    RewardOverlay(
                        rewardManager = rewardManager,
                        onDismiss = {
                            rewardManager.rewardCompleted()
                        }
                    )
                }
            }
        }
    }
}
