package com.example.mobile.presentation.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.mobile.R

/**
 * Utility class for showing reward popups and dialogs
 */
object RewardPopupUtil {

    /**
     * Shows a reward popup dialog
     */
    fun showRewardDialog(context: Context?, title: String, message: String) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    /**
     * Shows a coin reward popup
     */
    fun showCoinReward(context: Context?, amount: Int) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle("Reward Earned!")
                .setMessage("You earned $amount coins!")
                .setPositiveButton("Great!") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    /**
     * Shows a streak reward popup
     */
    fun showStreakReward(context: Context?, streak: Int, coins: Int) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle("Streak Milestone!")
                .setMessage("You've reached a $streak day streak!\nYou earned $coins coins and a reward chest!")
                .setPositiveButton("Awesome!") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    /**
     * Shows an achievement reward popup
     */
    fun showAchievementReward(context: Context?, achievementName: String, coins: Int) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle("Achievement Unlocked!")
                .setMessage("You unlocked '$achievementName'!\nYou earned $coins coins!")
                .setPositiveButton("Cool!") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    /**
     * Shows a level up reward popup
     */
    fun showLevelUpReward(context: Context?, level: Int, coins: Int) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle("Level Up!")
                .setMessage("Your pet reached level $level!\nYou earned $coins bonus coins!")
                .setPositiveButton("Nice!") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}