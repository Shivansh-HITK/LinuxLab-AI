package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.LinuxLabRepository
import com.example.data.UserProfile
import com.example.data.CompletedMission
import com.example.data.CommandMetric
import com.example.filesystem.VirtualFileSystem
import com.example.missions.Mission
import com.example.missions.MissionEngine
import com.example.terminal.CommandExecutor
import com.example.terminal.CommandResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isCommand: Boolean = false,
    val isSuccess: Boolean = true,
    val isSystemResult: Boolean = false,
    val suggestionTip: String? = null
)

class LinuxLabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LinuxLabRepository(application)
    private val sharedPrefs = application.getSharedPreferences("linuxlab_prefs", Context.MODE_PRIVATE)

    // Virtual File System State
    val fileSystem = VirtualFileSystem()

    // UI Navigation State
    var currentSubScreen = mutableStateOf("home") // "home", "terminal", "missions", "profile", "tutor"
    var hasCompletedOnboarding = mutableStateOf(sharedPrefs.getBoolean("onboarding_done", false))

    // Profile State
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val completedMissions: StateFlow<List<CompletedMission>> = repository.completedMissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val commandMetrics: StateFlow<List<CommandMetric>> = repository.commandMetrics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Interactive Terminal State
    val terminalLogs = mutableStateListOf<TerminalLog>()
    var terminalInput = mutableStateOf("")
    val terminalCommandHistory = mutableStateListOf<String>()
    var historyIndex = mutableStateOf(-1)

    // AI Tutor Chat History (Pair<Sender, Message> - Sender is "user" or "tutor")
    val tutorHistory = mutableStateListOf<Pair<String, String>>()
    var tutorInput = mutableStateOf("")
    var isTutorLoading = mutableStateOf(false)

    // Gamification animations notification event streams
    var missionCompletedEvent = mutableStateOf<Mission?>(null) // Show premium achievement alert when set
    var levelUpEvent = mutableStateOf<Int?>(null) // Show level up animation when set
    var errorNotificationEvent = mutableStateOf<String?>(null) // Notification banner

    init {
        // Hydrate terminal logs with welcome information
        resetSessionLogs()
        
        // Add greeting message to AI Tutor chat
        tutorHistory.add(Pair("tutor", "Salutations, Cadet! 🚀 I am your LinuxLab AI tutor. I can break down heavy parameters, explain directories, debug CLI error messages, and prepare you for dev operations.\n\nWhat Linux concept should we decode today?"))
    }

    private fun resetSessionLogs() {
        terminalLogs.clear()
        terminalLogs.add(TerminalLog(text = "LinuxLab Kernel v3.12.0-AI-READY", isSystemResult = true))
        terminalLogs.add(TerminalLog(text = "Loading fully secure virtual sandbox container environment...", isSystemResult = true))
        terminalLogs.add(TerminalLog(text = "Success. All system ports online. Type 'help' to see active guidelines.\n", isSystemResult = true))
    }

    fun completeOnboarding(name: String) {
        viewModelScope.launch {
            repository.updateProfileName(name)
            // Save state
            sharedPrefs.edit().putBoolean("onboarding_done", true).apply()
            hasCompletedOnboarding.value = true
            repository.awardXP(100) // 100 XP starting onboarding gift
        }
    }

    fun submitTerminalCommand() {
        val rawInput = terminalInput.value
        if (rawInput.trim().isEmpty()) return

        terminalInput.value = ""
        
        // Add to historical index and state
        terminalCommandHistory.add(rawInput)
        historyIndex.value = -1 // Reset

        // Add user statement to logs
        val promptSymbol = "cadet@linuxlab:${fileSystem.currentDir.getPath()}$ "
        terminalLogs.add(TerminalLog(text = promptSymbol + rawInput, isCommand = true))

        viewModelScope.launch {
            val result = CommandExecutor.execute(rawInput, fileSystem) { helpCmd ->
                GeminiService.generateContent(
                    prompt = "Explain the Linux command '$helpCmd'. Provide purpose, syntax, popular parameters, and 3 rich examples.",
                    systemInstruction = "You are LinuxLab AI, an engaging developer terminal helper tutor instructing a beginner student."
                )
            }

            if (result.isCleared) {
                terminalLogs.clear()
            } else {
                // Split results by lines and append to view logs
                terminalLogs.add(TerminalLog(
                    text = result.output, 
                    isSuccess = result.isSuccess,
                    suggestionTip = result.errorTip
                ))
            }

            // Save stat metrics to Room DB
            val baseCommand = rawInput.trim().split(" ").firstOrNull()?.lowercase() ?: ""
            repository.recordCommandMetrics(baseCommand, result.isSuccess)

            if (!result.isSuccess) {
                repository.incrementErrorsCorrected()
                if (result.errorTip != null) {
                    errorNotificationEvent.value = "Incorrect command parsed. Smart suggestion tip added below!"
                }
            }

            // Trigger Missions Val Checker
            checkCompletedMissions()
        }
    }

    fun populateInputFromPreset(presetCmd: String) {
        terminalInput.value = presetCmd
    }

    fun selectHistoryPrevious() {
        if (terminalCommandHistory.isEmpty()) return
        if (historyIndex.value == -1) {
            historyIndex.value = terminalCommandHistory.size - 1
        } else if (historyIndex.value > 0) {
            historyIndex.value -= 1
        }
        terminalInput.value = terminalCommandHistory[historyIndex.value]
    }

    fun selectHistoryNext() {
        if (terminalCommandHistory.isEmpty()) return
        if (historyIndex.value != -1 && historyIndex.value < terminalCommandHistory.size - 1) {
            historyIndex.value += 1
            terminalInput.value = terminalCommandHistory[historyIndex.value]
        } else {
            historyIndex.value = -1
            terminalInput.value = ""
        }
    }

    /**
     * Scan database and filesystem state to mark newly eligible missions completed!
     */
    private suspend fun checkCompletedMissions() {
        val completedList = completedMissions.value.map { it.missionId }.toSet()
        val allMissions = MissionEngine.missions

        for (mission in allMissions) {
            if (!completedList.contains(mission.id)) {
                // If it passes check validation
                if (mission.checkValidation(fileSystem)) {
                    val leveledUp = repository.completeMission(mission.id, mission.xp)
                    
                    // Fire beautiful state events for Compose overlays!
                    missionCompletedEvent.value = mission
                    if (leveledUp) {
                        val newLevel = repository.getProfile().level
                        levelUpEvent.value = newLevel
                    }
                    break // Deliver one completion animation at a time to keep UI fluid
                }
            }
        }
    }

    fun clearNotifications() {
        missionCompletedEvent.value = null
        levelUpEvent.value = null
        errorNotificationEvent.value = null
    }

    fun submitTutorMessage() {
        val msg = tutorInput.value.trim()
        if (msg.isEmpty()) return

        tutorInput.value = ""
        tutorHistory.add(Pair("user", msg))
        isTutorLoading.value = true

        viewModelScope.launch {
            val systemPrompt = "You are LinuxLab AI Tutor, an advanced learning assistant designed to teach Linux and DevOps to beginners. Keep explanations extremely visual, simple, structured and engaging."
            val response = GeminiService.generateContent(prompt = msg, systemInstruction = systemPrompt)
            tutorHistory.add(Pair("tutor", response))
            isTutorLoading.value = false
        }
    }

    fun explainSessionError(errorCommand: String, errorMessage: String) {
        isTutorLoading.value = true
        currentSubScreen.value = "tutor"
        viewModelScope.launch {
            val prompt = "Why did this Linux error occur?\nCommand typed: $errorCommand\nError output: $errorMessage\nPlease explain what went wrong and suggest concrete corrections for a beginner."
            val response = GeminiService.generateContent(
                prompt = prompt,
                systemInstruction = "You are LinuxLab AI, a friendly developer mentor assisting a learner."
            )
            tutorHistory.add(Pair("tutor", response))
            isTutorLoading.value = false
        }
    }

    fun resetAllAppProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            fileSystem.resetToDefault()
            resetSessionLogs()
            tutorHistory.clear()
            tutorHistory.add(Pair("tutor", "Progress has been fully reset. Start fresh Cadet! 🛠️"))
        }
    }
}
