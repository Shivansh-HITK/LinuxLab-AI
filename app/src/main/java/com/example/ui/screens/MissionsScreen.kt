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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LinuxLabViewModel
import com.example.ui.components.*
import com.example.missions.Mission
import com.example.missions.MissionEngine

@Composable
fun MissionsScreen(
    viewModel: LinuxLabViewModel,
    modifier: Modifier = Modifier
) {
    val completedList by viewModel.completedMissions.collectAsState()
    val completedIds = remember(completedList) { completedList.map { it.missionId }.toSet() }
    
    var selectedMission by remember { mutableStateOf<Mission?>(null) }
    var expandedHintsIndex by remember { mutableIntStateOf(-1) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "DevOps Missions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextMatteWhite,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                text = "Select a quest below. Open the terminal screen and complete the simulated requirements to satisfy the check scripts and gain XP points!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedGray,
                lineHeight = 18.sp
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(MissionEngine.missions) { mission ->
                    val isCompleted = completedIds.contains(mission.id)
                    
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMission = mission
                                expandedHintsIndex = -1
                            }
                            .testTag("mission_item_${mission.id}"),
                        borderColor = if (isCompleted) SoftCyan.copy(alpha = 0.4f) else BorderGlass,
                        backgroundAlpha = if (isCompleted) 0.45f else 0.55f
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Completion Icon Indicator
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isCompleted) SoftCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Completion indicator",
                                    tint = if (isCompleted) SoftCyan else TextMutedGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mission.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextMatteWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Category: ${mission.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentPurpleViolet
                                )
                            }

                            // Points indicator
                            SleekBadge(
                                text = "+${mission.xp} XP",
                                textColor = if (isCompleted) TextMutedGray else SoftCyan,
                                backgroundColor = if (isCompleted) Color.White.copy(alpha = 0.02f) else SoftCyan.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            }
        }

        // Animated Info Modal Bottom Sheet when mission selected
        AnimatedVisibility(
            visible = selectedMission != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedMission?.let { mission ->
                val isCompleted = completedIds.contains(mission.id)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(1.dp, BorderGlass, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .testTag("mission_detail_panel"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F0C20)
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Slider handle top indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(40.dp, 4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mission.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextMatteWhite,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(onClick = { selectedMission = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close detailed panel", tint = TextMutedGray)
                            }
                        }

                        // Short description
                        Text(
                            text = mission.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedGray,
                            lineHeight = 18.sp
                        )

                        Divider(color = BorderGlass)

                        // Action requirements
                        Text(
                            text = "Sandbox Instructions:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurpleViolet
                        )

                        Text(
                            text = mission.instructions,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMatteWhite,
                            lineHeight = 18.sp
                        )

                        Divider(color = BorderGlass)

                        // Custom tutor helper Hints segment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI Coach Hints:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SoftCyan
                            )

                            TextButton(
                                onClick = {
                                    expandedHintsIndex = if (expandedHintsIndex == -1) 0 else -1
                                }
                            ) {
                                Text(
                                    text = if (expandedHintsIndex == -1) "Reveal Hint" else "Hide Details",
                                    color = SoftCyan
                                )
                            }
                        }

                        AnimatedVisibility(visible = expandedHintsIndex != -1) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(16.dp)
                            ) {
                                mission.hints.forEachIndexed { idx, hint ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${idx+1}.",
                                            fontWeight = FontWeight.Bold,
                                            color = SoftCyan,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = hint,
                                            color = TextMatteWhite,
                                            style = MaterialTheme.typography.bodySmall,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Mission Start action link
                        Button(
                            onClick = {
                                viewModel.currentSubScreen.value = "terminal"
                                selectedMission = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) Color.White.copy(alpha = 0.1f) else AccentIndigo
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = "Active action button logo",
                                tint = if (isCompleted) SoftCyan else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCompleted) "Mission Complete (Play Sandbox)" else "Execute and Complete in Terminal",
                                color = if (isCompleted) SoftCyan else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
