package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LinuxLabViewModel
import com.example.ui.TerminalLog
import com.example.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun TerminalScreen(
    viewModel: LinuxLabViewModel,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var input by viewModel.terminalInput
    val logs = viewModel.terminalLogs
    val currentPath = viewModel.fileSystem.currentDir.getPath()

    val focusRequester = remember { FocusRequester() }

    // Scroll automatically to latest logs when sizes change
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(logs.size - 1)
            }
        }
    }

    // Capture initial keyboard focus safely
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Presets Scroll Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Quick Shortcut Command presets Finder",
                tint = SoftCyan,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Preset Commands:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedGray,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ls -la", "pwd", "cat welcome.txt", "help grep").forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                        .clickable { viewModel.populateInputFromPreset(preset) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMatteWhite
                    )
                }
            }
        }

        // Full terminal panel wrapper
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            cornerRadius = 16.dp,
            borderColor = Color.White.copy(alpha = 0.15f),
            backgroundAlpha = 0.55f
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header bar of the simulated terminal box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5F56), CircleShape))
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFFFFBD2E), CircleShape))
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF27C93F), CircleShape))
                    }
                    
                    Text(
                        text = "linuxlab-auth@ubuntu: $currentPath",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedGray
                    )

                    // Reset terminal button
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh console simulation session",
                        tint = TextMutedGray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { viewModel.resetAllAppProgress() }
                    )
                }

                // Interactive Outputs history
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (log.isCommand) {
                                // Commands display with bright cyan and custom terminal marker
                                Text(
                                    text = log.text,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftCyan
                                )
                            } else if (log.isSystemResult) {
                                // Greeters state
                                Text(
                                    text = log.text,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = TextMutedGray
                                )
                            } else {
                                // Dynamic standard outputs
                                Text(
                                    text = log.text,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (log.isSuccess) TextMatteWhite else Color(0xFFFCA5A5)
                                )

                                // Add smart assistant diagnostics banner underneath failed outputs
                                if (log.suggestionTip != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF8B5CF6).copy(alpha = 0.08f))
                                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Psychology,
                                                    contentDescription = "AI Tutor suggestion explanation bubble",
                                                    tint = Color(0xFFA78BFA),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "AI Smart Coach Diagnostics:",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFA78BFA)
                                                )
                                            }
                                            Text(
                                                text = log.suggestionTip,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMatteWhite,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive user console input block
                Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Command history nav up/down shortcuts
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Historical command prior",
                        tint = TextMutedGray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.selectHistoryPrevious() }
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Historical command next",
                        tint = TextMutedGray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.selectHistoryNext() }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "cadet@linuxlab:$currentPath$ ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = AccentIndigo,
                        fontWeight = FontWeight.Bold
                    )

                    // Basic TextField with customized glow cursor
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("terminal_input"),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = TextMatteWhite
                        ),
                        cursorBrush = SolidColor(SoftCyan),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { viewModel.submitTerminalCommand() }
                        ),
                        singleLine = true
                    )

                    if (input.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Execute command submit button",
                            tint = SoftCyan,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SoftCyan.copy(alpha = 0.15f))
                                .clickable { viewModel.submitTerminalCommand() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
