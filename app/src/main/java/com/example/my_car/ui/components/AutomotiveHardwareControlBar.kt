package com.example.my_car.ui.components

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import com.example.my_car.ui.theme.AutomotiveBluePrimaryLight
import com.example.my_car.ui.theme.AutomotiveCyanAccent

// 🚗 ROCK-SOLID HARDWARE HELPER FOR CAR HEAD UNITS & SMARTPHONES
object CarHardwareHelper {

    fun setVolume(context: Context, audioManager: AudioManager, targetVolume: Int, maxVolume: Int) {
        val safeVol = targetVolume.coerceIn(0, maxVolume)

        // 1. Standard Android AudioManager Streams
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

        // 2. Car Head Unit Vendor MCU Broadcast Intents (Topway, TS10, FYT, Microntek, Joying, FlyAudio)
        val carVolumeIntents = listOf(
            Intent("android.media.VOLUME_CHANGED_ACTION").apply {
                putExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", AudioManager.STREAM_MUSIC)
                putExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", safeVol)
                putExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", safeVol)
            },
            Intent("com.microntek.volume.change").apply {
                putExtra("volume", safeVol)
                putExtra("max_volume", maxVolume)
            },
            Intent("com.ts.intent.action.VOLUME_CHANGE").apply {
                putExtra("volume", safeVol)
            },
            Intent("com.flyaudio.intent.action.VOLUME_CHANGED").apply {
                putExtra("volume", safeVol)
            }
        )

        for (intent in carVolumeIntents) {
            try {
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setBrightness(context: Context, targetBrightness: Float) {
        val safeB = targetBrightness.coerceIn(0.05f, 1.0f)
        val bInt = (safeB * 255).toInt().coerceIn(12, 255)

        // 1. Current Window Attributes (Works 100% on Smartphones & App Windows)
        (context as? Activity)?.let { act ->
            try {
                val lp = act.window.attributes
                lp.screenBrightness = safeB
                act.window.attributes = lp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. System Hardware Backlight (Requires WRITE_SETTINGS on API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                try {
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
        } else {
            try {
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

        // 3. Car Head Unit MCU Brightness Intents
        val carBrightnessIntents = listOf(
            Intent("com.ts.intent.action.BRIGHTNESS_CHANGE").apply {
                putExtra("brightness", bInt)
            },
            Intent("com.microntek.brightness.change").apply {
                putExtra("brightness", bInt)
            }
        )

        for (intent in carBrightnessIntents) {
            try {
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

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

    var isMuted by remember { mutableStateOf(currentVolume == 0) }
    var previousVolume by remember { mutableIntStateOf(if (currentVolume > 0) currentVolume else (maxVolume / 2)) }

    // Initial Screen Brightness Reading
    var brightness by remember {
        val initialB = try {
            val currentSysB = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            (currentSysB / 255f).coerceIn(0.1f, 1.0f)
        } catch (e: Exception) {
            0.75f
        }
        mutableFloatStateOf(initialB)
    }

    // ⚡ LIVE SYNC RECEIVER: Steering Wheel Knobs & Physical Car Volume Controls
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
            addAction("com.microntek.volume.change")
            addAction("com.ts.intent.action.VOLUME_CHANGE")
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val liveVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                currentVolume = liveVol
                isMuted = liveVol == 0
            }
        }

        try {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                        if (isMuted || currentVolume == 0) {
                            val restoreVol = previousVolume.coerceAtLeast(1)
                            currentVolume = restoreVol
                            isMuted = false
                            CarHardwareHelper.setVolume(context, audioManager, restoreVol, maxVolume)
                        } else {
                            previousVolume = currentVolume
                            currentVolume = 0
                            isMuted = true
                            CarHardwareHelper.setVolume(context, audioManager, 0, maxVolume)
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
                            val volInt = newVol.toInt()
                            currentVolume = volInt
                            isMuted = volInt == 0
                            CarHardwareHelper.setVolume(context, audioManager, volInt, maxVolume)
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
                    modifier = Modifier
                        .size(20.dp)
                        .padding(start = 2.dp)
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
                            brightness = newB
                            CarHardwareHelper.setBrightness(context, newB)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
