package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LinuxLabViewModel
import com.example.ui.components.*

@Composable
fun OnboardingScreen(
    viewModel: LinuxLabViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    var userNameInput by remember { mutableStateOf("") }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val progressAnim by animateFloatAsState(
        targetValue = step / 3.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LinuxLabGradientBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Progress indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "LinuxLab AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AccentIndigo,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressAnim)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(AccentIndigo, NeonFuchsia)
                                )
                            )
                    )
                }
            }

            // Slide Contents
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        slideInHorizontally { width -> width / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                    },
                    label = "OnboardingStep"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> WelcomeSlide()
                        2 -> ExplainLinuxSlide()
                        3 -> SandboxPromptSlide(
                            value = userNameInput,
                            onValueChange = { userNameInput = it },
                            onSubmit = {
                                if (userNameInput.trim().isNotEmpty()) {
                                    keyboardController?.hide()
                                    viewModel.completeOnboarding(userNameInput.trim())
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Navigation Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(
                        onClick = { step -= 1 }
                    ) {
                        Text("Back", color = TextMutedGray)
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step += 1
                        } else {
                            if (userNameInput.trim().isNotEmpty()) {
                                keyboardController?.hide()
                                viewModel.completeOnboarding(userNameInput.trim())
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentIndigo
                    ),
                    modifier = Modifier.testTag("onboarding_next_button").height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = step < 3 || userNameInput.trim().isNotEmpty()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (step == 3) "Start Lab" else "Continue",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continue action",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeSlide() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAnimation"
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .background(AccentIndigo.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, AccentIndigo.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Interactive Terminal Logo",
                tint = SoftCyan,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to LinuxLab AI",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextMatteWhite,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Stop searching boring documentation tutorials. Master authentic DevOps and Linux scripting inside an animated glass simulator, backed by Gemini AI.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextMutedGray,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ExplainLinuxSlide() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Interactive Pedagogy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextMatteWhite,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OnboardingFeatureCard(
                icon = Icons.Default.Computer,
                title = "Virtual Sandbox",
                description = "Complete safety container mimicking standard directories. Practice creations risk-free."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.School,
                title = "AI Coaching Tutor",
                description = "Real-time query breakdown. Instant diagnostics explaining typos and command parameters."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.Code,
                title = "Gamified Missions",
                description = "Level up, secure badges, earn XP bonuses, and tracks streaks while mastering shells."
            )
        }
    }
}

@Composable
fun OnboardingFeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    GlassCard(
        cornerRadius = 14.dp,
        borderWidth = 1.dp,
        backgroundAlpha = 0.4f,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AccentIndigo.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, AccentIndigo.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = SoftCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextMatteWhite
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedGray
                )
            }
        }
    }
}

@Composable
fun SandboxPromptSlide(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Create Your Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextMatteWhite,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "Every great developer needs a shell username. Choose your lab callsign to initialize the container environment.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMutedGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        GlassCard(
            cornerRadius = 16.dp,
            backgroundAlpha = 0.5f,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "cadet_auth_register",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentPurpleViolet
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    placeholder = { Text("E.g. RootCadet", color = TextMutedGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextMatteWhite,
                        unfocusedTextColor = TextMatteWhite,
                        focusedBorderColor = SoftCyan,
                        unfocusedBorderColor = BorderGlass,
                        focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.1f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(if (value.isNotEmpty()) SoftCyan else Color.Red.copy(alpha = 0.7f), CircleShape)
                    )
                    Text(
                        text = if (value.isNotEmpty()) "Auth validation clean! Press start." else "Awaiting valid inputs key...",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (value.isNotEmpty()) SoftCyan else TextMutedGray
                    )
                }
            }
        }
    }
}
