package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var description: String = "",
    var icon: String = "",
    var targetValue: Int = 0,
    var rewardCoins: Int = 0,
    var rewardExp: Int = 0,
    var rewardChestType: String? = null,
    var isUnlocked: Boolean = false,
    var isClaimed: Boolean = false,
    var unlockedDate: Long? = null // timestamp
)