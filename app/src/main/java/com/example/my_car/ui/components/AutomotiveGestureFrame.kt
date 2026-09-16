package com.example.my_car.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.my_car.ui.theme.AutomotiveBluePrimaryLight
import com.example.my_car.ui.theme.AutomotiveCyanAccent

@Composable
fun AutomotiveGestureFrame(
    isPlaying: Boolean,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Ambient Lighting Border Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ambientBorder")
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isPlaying) 0.85f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = if (isPlaying) 2.dp else 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        if (isDark) AutomotiveCyanAccent.copy(alpha = alphaGlow) else AutomotiveBluePrimaryLight.copy(alpha = alphaGlow),
                        Color(0xFF7C4DFF).copy(alpha = alphaGlow)
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        // Main Screen Content (100% Unobstructed Touch Input)
        content()
    }
}
