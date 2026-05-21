package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 0,
    var name: String = "Linux Cadet",
    var level: Int = 1,
    var xp: Int = 0,
    var streak: Int = 0,
    var lastActiveTimestamp: Long = 0L,
    var errorsCorrectedCount: Int = 0,
    var commandMasteryScore: Int = 0
)

@Entity(tableName = "completed_missions")
data class CompletedMission(
    @PrimaryKey val missionId: String,
    val completedAt: Long = System.currentTimeMillis(),
    val pointsAwarded: Int = 0
)

@Entity(tableName = "command_metrics")
data class CommandMetric(
    @PrimaryKey val commandName: String,
    var totalExecuted: Int = 0,
    var successfulCount: Int = 0,
    var failedCount: Int = 0
)
