package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class LinuxLabRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val profileDao = db.userProfileDao
    private val missionDao = db.completedMissionDao
    private val metricsDao = db.commandMetricDao

    val userProfile: Flow<UserProfile> = profileDao.getProfileFlow().map { 
        it ?: UserProfile() // Return empty fallback which gets saved lazily
    }

    val completedMissions: Flow<List<CompletedMission>> = missionDao.getCompletedMissionsFlow()
    val commandMetrics: Flow<List<CommandMetric>> = metricsDao.getCommandMetricsFlow()

    suspend fun getProfile(): UserProfile {
        val existing = profileDao.getProfileDirect()
        if (existing == null) {
            val defaultProfile = UserProfile()
            profileDao.saveProfile(defaultProfile)
            return defaultProfile
        }
        return existing
    }

    suspend fun updateProfileName(newName: String) {
        val profile = getProfile()
        profile.name = newName
        profileDao.saveProfile(profile)
    }

    /**
     * Awards XP to the profile, updates level and streak, returns true if leveled up!
     */
    suspend fun awardXP(amount: Int): Boolean {
        val profile = getProfile()
        val oldLevel = profile.level
        profile.xp += amount
        
        // Let's decide standard level up formula: Level N needs N * 1000 total XP
        // e.g. Lvl 1: 0-999, Lvl 2: 1000-2999 (needs 2000), Lvl 3: 3000-5999
        // Simply: Level = (SquareRoot(XP / 500) + 1) or a clean linear progression:
        // Lvl 1 = 0 - 999 XP
        // Lvl 2 = 1000 - 1999 XP, etc.
        // Let's make it super simple: Level N = (XP / 800) + 1
        val newLevel = (profile.xp / 800) + 1
        profile.level = newLevel
        
        // Manage streak
        val currentTime = System.currentTimeMillis()
        val oldTimestamp = profile.lastActiveTimestamp
        if (oldTimestamp > 0) {
            val daysDiff = getDaysDifference(oldTimestamp, currentTime)
            if (daysDiff == 1) {
                profile.streak += 1
            } else if (daysDiff > 1) {
                profile.streak = 1 // Reset streak but keep at 1
            }
        } else {
            profile.streak = 1
        }
        profile.lastActiveTimestamp = currentTime
        
        profileDao.saveProfile(profile)
        return newLevel > oldLevel
    }

    suspend fun completeMission(missionId: String, xpBonus: Int): Boolean {
        val already = missionDao.getCompletedMissionsDirect().any { it.missionId == missionId }
        if (already) return false // Already completed

        missionDao.completeMission(CompletedMission(missionId, pointsAwarded = xpBonus))
        val leveledUp = awardXP(xpBonus)
        
        // Recalculate command mastery stats or simple indicator
        val profile = getProfile()
        profile.commandMasteryScore = (missionDao.getCompletedMissionsDirect().size * 10) + (profile.xp / 100)
        profileDao.saveProfile(profile)
        
        return leveledUp
    }

    suspend fun recordCommandMetrics(commandName: String, isSuccess: Boolean) {
        val metricsList = metricsDao.getCommandMetricsDirect()
        val metric = metricsList.find { it.commandName.equals(commandName, ignoreCase = true) } 
            ?: CommandMetric(commandName = commandName.lowercase())

        metric.totalExecuted += 1
        if (isSuccess) {
            metric.successfulCount += 1
        } else {
            metric.failedCount += 1
        }
        metricsDao.saveMetric(metric)
    }

    suspend fun incrementErrorsCorrected() {
        val profile = getProfile()
        profile.errorsCorrectedCount += 1
        profileDao.saveProfile(profile)
    }

    suspend fun resetAllProgress() {
        missionDao.deleteAllCompletedMissions()
        metricsDao.deleteAllMetrics()
        val defaultProfile = UserProfile(lastActiveTimestamp = System.currentTimeMillis())
        profileDao.saveProfile(defaultProfile)
    }

    private fun getDaysDifference(oldTime: Long, newTime: Long): Int {
        val oldCal = Calendar.getInstance().apply { timeInMillis = oldTime }
        val newCal = Calendar.getInstance().apply { timeInMillis = newTime }
        
        // Reset hours/minutes/seconds for day calculation
        oldCal.set(Calendar.HOUR_OF_DAY, 0)
        oldCal.set(Calendar.MINUTE, 0)
        oldCal.set(Calendar.SECOND, 0)
        oldCal.set(Calendar.MILLISECOND, 0)
        
        newCal.set(Calendar.HOUR_OF_DAY, 0)
        newCal.set(Calendar.MINUTE, 0)
        newCal.set(Calendar.SECOND, 0)
        newCal.set(Calendar.MILLISECOND, 0)
        
        val diff = newCal.timeInMillis - oldCal.timeInMillis
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
}
