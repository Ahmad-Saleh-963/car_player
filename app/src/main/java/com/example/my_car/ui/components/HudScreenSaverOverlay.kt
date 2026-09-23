package com.example.my_car.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.data.repository.VehicleTelemetryManager
import com.example.my_car.ui.theme.AutomotiveCyanAccent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HudScreenSaverOverlay(
    currentTrack: MediaTrack?,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry = VehicleTelemetryManager.telemetry

    // Ticking Live Clock State
    var timeDigitsText by remember { mutableStateOf("12:00:00") }
    var amPmText by remember { mutableStateOf("صباحاً") }
    var currentDateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val arLocale = Locale.forLanguageTag("ar")
        val timeFormat = SimpleDateFormat("hh:mm:ss", arLocale)
        val amPmFormat = SimpleDateFormat("a", arLocale)
        val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", arLocale)

        while (true) {
            val now = Date()
            timeDigitsText = timeFormat.format(now)
            amPmText = if (amPmFormat.format(now).contains("ص")) "صباحاً" else "مساءً"
            currentDateText = dateFormat.format(now)
            delay(500.milliseconds)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { onDismiss() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Digital Clock Bar
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "ساعة القيادة",
                                tint = AutomotiveCyanAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "$timeDigitsText $amPmText",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 32.sp
                                ),
                                color = AutomotiveCyanAccent
                            )
                        }

                        Text(
                            text = currentDateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Center HUD Large Speedometer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "عداد السرعة",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "${telemetry.speedKmh ?: 0}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 80.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "السرعة المباشرة (كم/س)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF00E5FF)
                        )
                    }

                    // Bottom Playing Track Banner & Touch Dismiss Hint
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentTrack != null) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = AutomotiveCyanAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentTrack.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${currentTrack.artist} • ${if (isPlaying) "شغال الآن 🎵" else "متوقف ⏸️"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "👈 إلمس الشاشة في أي مكان للعودة للمشغل",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
