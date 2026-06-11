package com.example.mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mobile.domain.GameEventRarity

@Entity(tableName = "game_events")
data class GameEventEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var type: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var title: String = "",
    var description: String = "",
    var icon: String = "",
    var rarity: String = GameEventRarity.COMMON.name,
    var payload: String? = null
)
