package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.UserProfile
import com.example.data.CommandMetric
import com.example.missions.MissionEngine

@Composable
fun HomeScreen(
    viewModel: LinuxLabViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val metrics by viewModel.commandMetrics.collectAsState()
    val completedCount = viewModel.completedMissions.collectAsState().value.size

    val totalMissions = MissionEngine.missions.size
    val levelProgress = profile.xp % 800
    val progressPercent = levelProgress.toFloat() / 800f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
    ) {
        // Welcoming header row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${profile.name} 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMatteWhite
                    )
                    Text(
                        text = "Initialize your terminal. Linux awaits.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedGray
                    )
                }

                // Level badge circular widget
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            Brush.linearGradient(listOf(AccentIndigo, AccentPurpleViolet)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lvl\n${profile.level}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        // Stats Row Widget (Dual Cards: XP & Streaks)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundAlpha = 0.5f
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${profile.streak} Days",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextMatteWhite
                            )
                            Text(
                                text = "Daily Streak",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedGray
                            )
                        }
                    }
                }

                // XP Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundAlpha = 0.5f
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE0F2FE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Experience Points",
                                tint = SoftCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${profile.xp} XP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextMatteWhite
                            )
                            Text(
                                text = "Earned Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedGray
                            )
                        }
                    }
                }
            }
        }

        // Level Linear Progress Bar Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundAlpha = 0.4f
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level PROGRESS",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurpleViolet,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$levelProgress / 800 XP for Lvl ${profile.level + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedGray
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = AccentIndigo,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            }
        }

        // Quick Launch Terminal Quick Link Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                AccentIndigo.copy(alpha = 0.85f),
                                AccentPurpleViolet.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .clickable { viewModel.currentSubScreen.value = "terminal" }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "OPEN LAB TERMINAL 💻",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Run directory navigations cd, mkdir, touch, cat simulations securely in sandbox.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Console Simulator",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // AI Coaching Recommendations & Prompts
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundAlpha = 0.5f
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Coach Recommendations",
                            tint = SoftCyan,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "AI Lab Tutor Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextMatteWhite
                        )
                    }

                    Divider(color = BorderGlass)

                    Text(
                        text = "Based on your rookie operations profile, I suggest mastering directories manipulation next. Try making relative paths folders, or configure system executables with chmod.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedGray,
                        lineHeight = 18.sp
                    )
                    
                    // Suggestion Action Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftCyan.copy(alpha = 0.12f))
                                .clickable {
                                    viewModel.currentSubScreen.value = "terminal"
                                    viewModel.populateInputFromPreset("help grep")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Explain grep 🔍", style = MaterialTheme.typography.bodySmall, color = SoftCyan)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentPurpleViolet.copy(alpha = 0.12f))
                                .clickable {
                                    viewModel.currentSubScreen.value = "terminal"
                                    viewModel.populateInputFromPreset("help chmod")
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Chmod Guide 🔐", style = MaterialTheme.typography.bodySmall, color = AccentPurpleViolet)
                        }
                    }
                }
            }
        }

        // Missions Progression card summary
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundAlpha = 0.5f,
                borderColor = BorderGlass
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lab Achievements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextMatteWhite
                        )

                        TextButton(
                            onClick = { viewModel.currentSubScreen.value = "missions" }
                        ) {
                            Text("All Missions", color = AccentIndigo)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Progress pie circle simulation
                        Box(
                            modifier = Modifier.size(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { completedCount.toFloat() / totalMissions.toFloat() },
                                modifier = Modifier.fillMaxSize(),
                                color = SoftCyan,
                                strokeWidth = 5.dp,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            Text(
                                text = "$completedCount/$totalMissions",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMatteWhite
                            )
                        }

                        Column {
                            Text(
                                text = "Mission Progress Status",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextMatteWhite
                            )
                            Text(
                                text = "Earn XP matching complete challenges validation inside the sandbox.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedGray
                            )
                        }
                    }
                }
            }
        }
    }
}
