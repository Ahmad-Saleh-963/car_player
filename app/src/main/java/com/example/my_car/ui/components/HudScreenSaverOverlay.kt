package com.example.my_car.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HudScreenSaverOverlay(
    currentTrack: MediaTrack?,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val telemetry = VehicleTelemetryManager.telemetry

    // Ticking Live Time & Angles State
    var hours by remember { mutableIntStateOf(12) }
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }

    var timeDigitsText by remember { mutableStateOf("12:00:00") }
    var amPmText by remember { mutableStateOf("صباحاً") }
    var currentDateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val arLocale = Locale.forLanguageTag("ar")
        val timeFormat = SimpleDateFormat("hh:mm:ss", arLocale)
        val amPmFormat = SimpleDateFormat("a", arLocale)
        val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", arLocale)

        while (true) {
            val cal = Calendar.getInstance()
            hours = cal.get(Calendar.HOUR)
            minutes = cal.get(Calendar.MINUTE)
            seconds = cal.get(Calendar.SECOND)

            val now = cal.time
            timeDigitsText = timeFormat.format(now)
            amPmText = if (amPmFormat.format(now).contains("ص")) "صباحاً" else "مساءً"
            currentDateText = dateFormat.format(now)
            delay(250.milliseconds)
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
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Center Row: Enlarged Analog Clock + Digital Speedometer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Luxurious Enlarged Analog Dashboard Clock (200.dp)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0F172A),
                                border = BorderStroke(2.5.dp, AutomotiveCyanAccent.copy(alpha = 0.85f)),
                                shadowElevation = 10.dp,
                                modifier = Modifier.size(200.dp)
                            ) {
                                CanvasAnalogClock(
                                    hours = hours,
                                    minutes = minutes,
                                    seconds = seconds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Enlarged Digital Time Text (22.sp)
                            Text(
                                text = "$timeDigitsText $amPmText",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 22.sp
                                ),
                                color = AutomotiveCyanAccent
                            )

                            // Arabic Day & Date Display directly below clock
                            Text(
                                text = currentDateText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(180.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        )

                        // 2. Large Digital Speedometer Gauge
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(2.dp, Color(0xFF00E5FF).copy(alpha = 0.85f)),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "السرعة",
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(42.dp)
                                        )
                                        Text(
                                            text = "${telemetry.speedKmh ?: 0}",
                                            style = MaterialTheme.typography.displayLarge.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 72.sp,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = "السرعة المباشرة (كم/س)",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = Color(0xFF00E5FF)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Playing Track Banner (SHOWS ONLY WHEN MUSIC IS ACTIVELY PLAYING) & Touch Dismiss Hint
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentTrack != null && isPlaying) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, AutomotiveCyanAccent.copy(alpha = 0.4f)),
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
                                            text = "${currentTrack.artist} • شغال الآن 🎵",
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
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

// 🕒 LUXURIOUS AUTOMOTIVE CANVAS ANALOG CLOCK WITH ANIMATED HANDS
@Composable
fun CanvasAnalogClock(
    hours: Int,
    minutes: Int,
    seconds: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = AutomotiveCyanAccent
    val secondHandColor = Color(0xFFFF1744)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val radius = (width.coerceAtMost(height) / 2f) * 0.88f
        val center = Offset(width / 2f, height / 2f)

        // 1. Draw Outer Dial Ring
        drawCircle(
            color = primaryColor.copy(alpha = 0.25f),
            radius = radius,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )

        // 2. Draw 12 Hour Ticks
        for (i in 0 until 12) {
            val angleDeg = i * 30f - 90f
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val startR = radius * 0.82f
            val endR = radius * 0.95f

            val startX = center.x + startR * cos(angleRad).toFloat()
            val startY = center.y + startR * sin(angleRad).toFloat()
            val endX = center.x + endR * cos(angleRad).toFloat()
            val endY = center.y + endR * sin(angleRad).toFloat()

            drawLine(
                color = primaryColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (i % 3 == 0) 3.5.dp.toPx() else 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 3. Hour Hand
        val hourAngleDeg = (hours % 12 + minutes / 60f) * 30f - 90f
        val hourAngleRad = Math.toRadians(hourAngleDeg.toDouble())
        val hourR = radius * 0.50f
        val hourX = center.x + hourR * cos(hourAngleRad).toFloat()
        val hourY = center.y + hourR * sin(hourAngleRad).toFloat()

        drawLine(
            color = Color.White,
            start = center,
            end = Offset(hourX, hourY),
            strokeWidth = 4.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 4. Minute Hand
        val minAngleDeg = (minutes + seconds / 60f) * 6f - 90f
        val minAngleRad = Math.toRadians(minAngleDeg.toDouble())
        val minR = radius * 0.72f
        val minX = center.x + minR * cos(minAngleRad).toFloat()
        val minY = center.y + minR * sin(minAngleRad).toFloat()

        drawLine(
            color = primaryColor,
            start = center,
            end = Offset(minX, minY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 5. Second Hand
        val secAngleDeg = seconds * 6f - 90f
        val secAngleRad = Math.toRadians(secAngleDeg.toDouble())
        val secR = radius * 0.82f
        val secX = center.x + secR * cos(secAngleRad).toFloat()
        val secY = center.y + secR * sin(secAngleRad).toFloat()

        drawLine(
            color = secondHandColor,
            start = center,
            end = Offset(secX, secY),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 6. Center Pin Cap
        drawCircle(
            color = secondHandColor,
            radius = 4.5.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}
