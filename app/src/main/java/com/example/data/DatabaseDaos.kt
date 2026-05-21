package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 0")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 0")
    suspend fun getProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)
}

@Dao
interface CompletedMissionDao {
    @Query("SELECT * FROM completed_missions ORDER BY completedAt DESC")
    fun getCompletedMissionsFlow(): Flow<List<CompletedMission>>

    @Query("SELECT * FROM completed_missions")
    suspend fun getCompletedMissionsDirect(): List<CompletedMission>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun completeMission(mission: CompletedMission)

    @Query("DELETE FROM completed_missions")
    suspend fun deleteAllCompletedMissions()
}

@Dao
interface CommandMetricDao {
    @Query("SELECT * FROM command_metrics ORDER BY totalExecuted DESC")
    fun getCommandMetricsFlow(): Flow<List<CommandMetric>>

    @Query("SELECT * FROM command_metrics")
    suspend fun getCommandMetricsDirect(): List<CommandMetric>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMetric(metric: CommandMetric)

    @Query("DELETE FROM command_metrics")
    suspend fun deleteAllMetrics()
}
