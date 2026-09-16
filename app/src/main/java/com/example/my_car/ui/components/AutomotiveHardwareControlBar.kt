package com.example.my_car.ui.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_car.ui.theme.AutomotiveBluePrimaryLight
import com.example.my_car.ui.theme.AutomotiveCyanAccent

@Composable
fun AutomotiveHardwareControlBar(
    modifier: Modifier = Modifier,
    isVerticalLayout: Boolean = false
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    val maxVolume = remember(audioManager) {
        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    }

    var currentVolume by remember {
        mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))
    }

    var isMuted by remember { mutableStateOf(false) }
    var previousVolume by remember { mutableIntStateOf(currentVolume) }

    var brightness by remember {
        mutableFloatStateOf(0.75f)
    }

    // Failsafe Volume Adjustment Function for Android Head Units & Legacy Android 5.0+
    val setCarVolume = { volInt: Int ->
        val safeVol = volInt.coerceIn(0, maxVolume)
        currentVolume = safeVol
        isMuted = safeVol == 0

        try {
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                safeVol,
                AudioManager.FLAG_SHOW_UI
            )
            audioManager.setStreamVolume(
                AudioManager.STREAM_SYSTEM,
                safeVol,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Failsafe System Hardware Brightness Adjustment for Android Head Units
    val setCarBrightness = { newB: Float ->
        val safeB = newB.coerceIn(0.05f, 1.0f)
        brightness = safeB

        // 1. Update Current Activity Window Attributes
        (context as? Activity)?.let { act ->
            val lp = act.window.attributes
            lp.screenBrightness = safeB
            act.window.attributes = lp
        }

        // 2. Hardware System Backlight Value (0..255) for Legacy Android 5.0+ & Car Screen Backlights
        try {
            val bInt = (safeB * 255).toInt().coerceIn(12, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                bInt
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val volumeCard = @Composable { cardModifier: Modifier ->
        Surface(
            modifier = cardModifier
                .border(
                    1.dp,
                    if (isDark) AutomotiveCyanAccent.copy(alpha = 0.4f) else AutomotiveBluePrimaryLight.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        if (isMuted) {
                            val restoreVol = previousVolume.coerceAtLeast(1)
                            setCarVolume(restoreVol)
                        } else {
                            previousVolume = currentVolume
                            setCarVolume(0)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted || currentVolume == 0) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "كتم الصوت",
                        tint = if (isMuted || currentVolume == 0) Color(0xFFFF5252) else (if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔊 الصوت",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(currentVolume * 100 / maxVolume)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight
                        )
                    }

                    Slider(
                        value = currentVolume.toFloat(),
                        onValueChange = { newVol ->
                            setCarVolume(newVol.toInt())
                        },
                        valueRange = 0f..maxVolume.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                            activeTrackColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }
    }

    val brightnessCard = @Composable { cardModifier: Modifier ->
        Surface(
            modifier = cardModifier
                .border(
                    1.dp,
                    Color(0xFFFFB300).copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "الإضاءة",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(20.dp).padding(start = 2.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "☀️ الإضاءة",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(brightness * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFFB300)
                        )
                    }

                    Slider(
                        value = brightness,
                        onValueChange = { newB ->
                            setCarBrightness(newB)
                        },
                        valueRange = 0.05f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB300),
                            activeTrackColor = Color(0xFFFFB300)
                        ),
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }
    }

    if (isVerticalLayout) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            volumeCard(Modifier.fillMaxWidth())
            brightnessCard(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            volumeCard(Modifier.weight(1f))
            brightnessCard(Modifier.weight(1f))
        }
    }
}
