package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LinuxLabViewModel
import com.example.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun TutorScreen(
    viewModel: LinuxLabViewModel,
    modifier: Modifier = Modifier
) {
    val tutorHistory = viewModel.tutorHistory
    var tutorInput by viewModel.tutorInput
    val isLoading by viewModel.isTutorLoading
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll to lowest chats
    LaunchedEffect(tutorHistory.size) {
        if (tutorHistory.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(tutorHistory.size - 1)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick starters chips finder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Quick Query Presets",
                tint = SoftCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Tutor Starters:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedGray,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Why do we need Linux?",
                "What is permission 755?"
            ).forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                        .clickable { 
                            tutorInput = preset
                            viewModel.submitTutorMessage()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMatteWhite
                    )
                }
            }
        }

        // Chat viewport container
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            cornerRadius = 16.dp,
            backgroundAlpha = 0.5f
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive conversation stack
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tutorHistory) { chat ->
                        val isUser = chat.first == "user"
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(AccentIndigo.copy(alpha = 0.15f), CircleShape)
                                        .border(1.dp, AccentIndigo.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "AI Logo",
                                        tint = SoftCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // Conversation text bubble
                            Box(
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isUser) 14.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 14.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) AccentIndigo.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isUser) AccentIndigo.copy(alpha = 0.6f) else BorderGlass,
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isUser) 14.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 14.dp
                                        )
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Parse for monospace block text if containing code highlights
                                    val formattedContent = chat.second
                                    val hasCodeBlock = formattedContent.contains("```") || formattedContent.contains("`")
                                    
                                    if (hasCodeBlock) {
                                        // Standard formatted block split
                                        Text(
                                            text = formattedContent,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMatteWhite,
                                            lineHeight = 18.sp,
                                            fontFamily = if (formattedContent.startsWith("`")) FontFamily.Monospace else FontFamily.Default
                                        )
                                    } else {
                                        Text(
                                            text = formattedContent,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMatteWhite,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }

                            if (isUser) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "User Avatar short placeholder",
                                        tint = TextMatteWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Shimmer loader if waiting API
                    if (isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = SoftCyan,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("AI is compiling...", style = MaterialTheme.typography.bodySmall, color = TextMutedGray)
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = BorderGlass)

                // Typing inputs drawer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tutorInput,
                        onValueChange = { tutorInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tutor_input"),
                        placeholder = { Text("Ask your coaching assistant...", color = TextMutedGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextMatteWhite,
                            unfocusedTextColor = TextMatteWhite,
                            focusedBorderColor = SoftCyan,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.05f)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                keyboardController?.hide()
                                viewModel.submitTutorMessage()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.submitTutorMessage()
                        },
                        enabled = tutorInput.isNotEmpty() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (tutorInput.isNotEmpty()) SoftCyan else Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send text",
                            tint = if (tutorInput.isNotEmpty()) Color.Black else TextMutedGray
                        )
                    }
                }
            }
        }
    }
}
