package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
fun ProfileScreen(
    viewModel: LinuxLabViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val metrics by viewModel.commandMetrics.collectAsState()
    val completedList by viewModel.completedMissions.collectAsState()
    val completedCount = completedList.size
    
    val totalMissions = MissionEngine.missions.size
    val completionPercent = if (totalMissions > 0) (completedCount.toFloat() / totalMissions.toFloat() * 100).toInt() else 0

    // Compute Weak Areas (Commands with failedCount > 0, sorted by fail weight)
    val weakCommands = remember(metrics) {
        metrics.filter { it.failedCount > 0 }
            .sortedByDescending { it.failedCount.toFloat() / it.totalExecuted.toFloat() }
    }

    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
    ) {
        // Cadet card header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                backgroundAlpha = 0.6f
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = "User Avatar profile picture placeholder",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextMatteWhite
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTAINER STATUS: ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Stats summary metrics checklist
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // completion rate
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundAlpha = 0.4f
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$completionPercent%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftCyan
                        )
                        Text(
                            text = "Completion Rate",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // errors corrected
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundAlpha = 0.4f
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${profile.errorsCorrectedCount}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AccentPurpleViolet
                        )
                        Text(
                            text = "Mistakes Corrected",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMutedGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Weak topics alert card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundAlpha = 0.5f,
                borderColor = if (weakCommands.isNotEmpty()) Color.Yellow.copy(alpha = 0.25f) else BorderGlass
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Threat / Analysis Weak topic metrics warning symbol",
                            tint = if (weakCommands.isNotEmpty()) Color.Yellow else TextMutedGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Command Vulnerabilities Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextMatteWhite
                        )
                    }

                    Divider(color = BorderGlass)

                    if (weakCommands.isEmpty()) {
                        Text(
                            text = "Zero vulnerabilities detected in your executive flow! Your command execution compile records are fully green in this container session.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedGray
                        )
                    } else {
                        Text(
                            text = "These commands have generated errors during execution. Practice these flags in the Sandbox to strengthen terminal memory:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMutedGray
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            weakCommands.take(3).forEach { vuln ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(Color.Yellow, CircleShape))
                                        Text(
                                            text = vuln.commandName.uppercase(),
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = TextMatteWhite,
                                            fontSize = 13.sp
                                        )
                                    }
                                    
                                    val errorRate = (vuln.failedCount.toFloat() / vuln.totalExecuted.toFloat() * 100).toInt()
                                    Text(
                                        text = "$errorRate% failure weight (${vuln.failedCount} fails)",
                                        fontSize = 12.sp,
                                        color = TextMutedGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mastered / Total execution details metrics
        item {
            Text(
                text = "Command Session Logs Execution Metrics",
                style = MaterialTheme.typography.titleMedium,
                color = TextMatteWhite,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (metrics.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundAlpha = 0.4f
                ) {
                    Text(
                        text = "Awaiting first execution entries. Launch your lab simulator to record terminal logs!",
                        color = TextMutedGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            items(metrics) { metric ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundAlpha = 0.45f
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "shell: ${metric.commandName}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SoftCyan,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Total Run Calls: ${metric.totalExecuted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedGray
                            )
                        }

                        // Compact metrics indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Success",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedGray
                                )
                                Text(
                                    text = "${metric.successfulCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF86EFAC),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Failed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedGray
                                )
                                Text(
                                    text = "${metric.failedCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dangerous reset section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.15f)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(Color.Red.copy(alpha = 0.35f))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_progress_button")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Purge achievements icon", tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Purge & Reinitialize Container Logs", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Container Progress?", color = TextMatteWhite, fontWeight = FontWeight.ExtraBold) },
            text = { Text("Are you completely sure? This will permanently purge your earned XP points, level progression accomplishments, daily streaks, completed achievements history and reinitialize the file system back to default.", color = TextMutedGray) },
            containerColor = Color(0xFF0F0C20),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllAppProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Purge Logs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Abort", color = TextMutedGray)
                }
            }
        )
    }
}
