package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Color board matches Linear/Raycast dark theme:
val DarkObsidian = Color(0xFF0B0813)
val CardSlate = Color(0xFF141221).copy(alpha = 0.70f)
val AccentIndigo = Color(0xFF6366F1)
val AccentPurpleViolet = Color(0xFF8B5CF6)
val SoftCyan = Color(0xFF06B6D4)
val NeonFuchsia = Color(0xFFD946EF)
val BorderGlass = Color(0xFFFFFFFF).copy(alpha = 0.12f)
val TextMatteWhite = Color(0xFFF3F4F6)
val TextMutedGray = Color(0xFF9CA3AF)

@Composable
fun LinuxLabGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(DarkObsidian)
            .drawBehind {
                // Bottom fushia glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonFuchsia.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.9f),
                        radius = size.width * 0.6f
                    )
                )
                // Top-left violet soft light glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentPurpleViolet.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.15f),
                        radius = size.width * 0.7f
                    )
                )
                // Center-right indigo soft glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentIndigo.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.45f),
                        radius = size.width * 0.5f
                    )
                )
            }
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = BorderGlass,
    backgroundAlpha: Float = 0.65f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(CardSlate.copy(alpha = backgroundAlpha))
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun SleekBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AccentIndigo.copy(alpha = 0.15f),
    textColor: Color = AccentIndigo
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(),
    color: Color = TextMatteWhite,
    delayMillis: Int = 30
) {
    val displayedText = remember { Animatable(0f) }
    
    // Animate typing on text change
    LaunchedEffect(text) {
        displayedText.snapTo(0f)
        displayedText.animateTo(
            targetValue = text.length.toFloat(),
            animationSpec = tween(
                durationMillis = text.length * delayMillis
            )
        )
    }

    val charactersToShow = displayedText.value.toInt().coerceIn(0, text.length)
    Text(
        text = text.take(charactersToShow),
        style = style,
        color = color,
        modifier = modifier
    )
}
