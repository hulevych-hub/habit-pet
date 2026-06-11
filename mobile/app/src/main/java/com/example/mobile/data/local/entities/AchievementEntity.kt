package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey var id: String = "",
    var progress: Int = 0,
    var isUnlocked: Boolean = false,
    var isClaimed: Boolean = false,
    var unlockedDate: Long? = null
)
