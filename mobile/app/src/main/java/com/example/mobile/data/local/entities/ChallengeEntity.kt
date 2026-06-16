package com.example.mobile.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey var id: Long = 1,
    @ColumnInfo(name = "challenge_id") var challengeId: String,
    var title: String,
    var description: String,
    var icon: String,
    var type: String,
    @ColumnInfo(name = "target_value") var targetValue: Int,
    @ColumnInfo(name = "progress_value") var progressValue: Int = 0,
    @ColumnInfo(name = "reward_ids_json") var rewardIdsJson: String,
    @ColumnInfo(name = "is_completed") var isCompleted: Boolean = false,
    @ColumnInfo(name = "is_claimed") var isClaimed: Boolean = false,
    @ColumnInfo(name = "previous_challenge_id") var previousChallengeId: String? = null,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at") var completedAt: Long? = null,
    @ColumnInfo(name = "claimed_at") var claimedAt: Long? = null
)
