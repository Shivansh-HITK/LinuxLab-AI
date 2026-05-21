package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LinuxLabViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LinuxLabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) { // Force beautiful dark mode
                val completedOnboarding by viewModel.hasCompletedOnboarding

                if (!completedOnboarding) {
                    OnboardingScreen(viewModel = viewModel)
                } else {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: LinuxLabViewModel) {
    val activeTab by viewModel.currentSubScreen
    
    // Notification Overlay states
    val completedMissionEvent by viewModel.missionCompletedEvent
    val levelUpEvent by viewModel.levelUpEvent
    val errorNotificationEvent by viewModel.errorNotificationEvent

    LinuxLabGradientBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Let the rich background gradient shine through!
            bottomBar = {
                // Frosted Navigation tabs row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    GlassCard(
                        cornerRadius = 24.dp,
                        borderWidth = 1.dp,
                        backgroundAlpha = 0.82f,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NavBarTab(
                                tag = "home",
                                label = "Home",
                                icon = Icons.Default.Dashboard,
                                isActive = activeTab == "home",
                                onClick = { viewModel.currentSubScreen.value = "home" }
                            )
                            NavBarTab(
                                tag = "terminal",
                                label = "Terminal",
                                icon = Icons.Default.Terminal,
                                isActive = activeTab == "terminal",
                                onClick = { viewModel.currentSubScreen.value = "terminal" }
                            )
                            NavBarTab(
                                tag = "missions",
                                label = "Missions",
                                icon = Icons.Default.AssignmentTurnedIn,
                                isActive = activeTab == "missions",
                                onClick = { viewModel.currentSubScreen.value = "missions" }
                            )
                            NavBarTab(
                                tag = "tutor",
                                label = "AI Tutor",
                                icon = Icons.Default.Psychology,
                                isActive = activeTab == "tutor",
                                onClick = { viewModel.currentSubScreen.value = "tutor" }
                            )
                            NavBarTab(
                                tag = "profile",
                                label = "Profile",
                                icon = Icons.Default.Analytics,
                                isActive = activeTab == "profile",
                                onClick = { viewModel.currentSubScreen.value = "profile" }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Sub screen router content animations
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { currentScreen ->
                    when (currentScreen) {
                        "home" -> HomeScreen(viewModel = viewModel)
                        "terminal" -> TerminalScreen(viewModel = viewModel)
                        "missions" -> MissionsScreen(viewModel = viewModel)
                        "tutor" -> TutorScreen(viewModel = viewModel)
                        "profile" -> ProfileScreen(viewModel = viewModel)
                    }
                }

                // Error Suggestions Banner Overlay
                AnimatedVisibility(
                    visible = errorNotificationEvent != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    errorNotificationEvent?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7F1D1D)) // deep crimson error look
                                .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.clearNotifications() }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Diagnostics threat alert",
                                    tint = Color.Red
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error notification banner",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Congratulatory Level Up Modal Overlay
                AnimatedVisibility(
                    visible = levelUpEvent != null,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    levelUpEvent?.let { newLvl ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.85f))
                                .clickable { viewModel.clearNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            GlassCard(
                                cornerRadius = 32.dp,
                                borderWidth = 1.dp,
                                borderColor = SoftCyan.copy(alpha = 0.5f),
                                backgroundAlpha = 0.9f,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .wrapContentHeight()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .background(
                                                Brush.radialGradient(listOf(SoftCyan, Color.Transparent)),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Level upgrade success icon representation badge",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }

                                    Text(
                                        text = "LEVEL UP! 🌟",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SoftCyan,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Text(
                                        text = "Congratulations Cadet! You have advanced your shell terminal execution score. You are now:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMutedGray,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "LAB LEVEL $newLvl OPERATOR",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Button(
                                        onClick = { viewModel.clearNotifications() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SoftCyan),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Continue Learning", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Congratulatory Completed Mission Card Overlay
                AnimatedVisibility(
                    visible = completedMissionEvent != null,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    completedMissionEvent?.let { mission ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.8f))
                                .clickable { viewModel.clearNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            GlassCard(
                                cornerRadius = 32.dp,
                                borderWidth = 1.dp,
                                borderColor = Color(0xFFD946EF).copy(alpha = 0.4f),
                                backgroundAlpha = 0.92f,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .wrapContentHeight()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(20.dp),
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .background(
                                                Brush.radialGradient(listOf(NeonFuchsia, Color.Transparent)),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = "Mission complete trophy medallion illustration",
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }

                                    Text(
                                        text = "MISSION ACCOMPLISHED! 🏆",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonFuchsia,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "Val validation check script successfully satisfied!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMutedGray,
                                        textAlign = TextAlign.Center
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = mission.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "+${mission.xp} XP points bonus credit added.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.clearNotifications() },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonFuchsia),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Collect Rewards", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.NavBarTab(
    tag: String,
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .clickable { onClick() }
            .testTag("tab_button_$tag"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(38.dp, 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isActive) AccentIndigo.copy(alpha = 0.2f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) SoftCyan else TextMutedGray,
                modifier = Modifier
                    .size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(3.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) TextMatteWhite else TextMutedGray,
            fontSize = 9.sp
        )
    }
}
