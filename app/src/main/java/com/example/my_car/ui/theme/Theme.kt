package com.example.my_car.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AutomotiveCyanAccent,
    secondary = AutomotiveBlueAccent,
    tertiary = AutomotiveVioletAccent,
    background = AutomotiveDarkBg,
    surface = AutomotiveDarkSurface,
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color.White,
    onBackground = AutomotiveTextPrimaryDark,
    onSurface = AutomotiveTextPrimaryDark,
    outline = AutomotiveDarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AutomotiveBluePrimaryLight,
    secondary = AutomotiveBluePrimaryLight,
    tertiary = AutomotiveBluePrimaryLight,
    background = AutomotiveLightBg,
    surface = AutomotiveLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = AutomotiveTextPrimaryLight,
    onSurface = AutomotiveTextPrimaryLight,
    outline = AutomotiveLightCardBorder
)

@Composable
fun My_carTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
