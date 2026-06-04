package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String = "",
    var icon: String = "",
    var type: String = "", // CHECKBOX or TIMER
    var minimumDurationMinutes: Int = 0,
    var currentStreak: Int = 0,
    var bestStreak: Int = 0
)