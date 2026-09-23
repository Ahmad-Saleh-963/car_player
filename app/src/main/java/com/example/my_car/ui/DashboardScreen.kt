package com.example.my_car.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_car.data.repository.VehicleTelemetryManager
import com.example.my_car.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

sealed class CarScreen {
    object Dashboard : CarScreen()
    object Audio : CarScreen()
    object Video : CarScreen()
    object Folders : CarScreen()
    object Favorites : CarScreen()
    object Settings : CarScreen()
}

@Composable
fun DashboardScreen(
    audioTracksCount: Int,
    videoTracksCount: Int,
    favoritesCount: Int,
    isTelemetryVisible: Boolean = true,
    onDismissTelemetry: () -> Unit = {},
    onNavigate: (CarScreen) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var isPrayerBannerVisible by remember { mutableStateOf(true) }

    // Live Clock State
    var timeDigitsText by remember { mutableStateOf("12:00") }
    var amPmText by remember { mutableStateOf("صباحاً") }
    var currentDateText by remember { mutableStateOf("") }

    // Live Real Vehicle Telemetry State
    val telemetry = VehicleTelemetryManager.telemetry

    // Ticking Clock Effect
    LaunchedEffect(Unit) {
        val arLocale = Locale.forLanguageTag("ar")
        val timeFormat = SimpleDateFormat("hh:mm", arLocale)
        val amPmFormat = SimpleDateFormat("a", arLocale)
        val dateFormat = SimpleDateFormat("EEEE، d MMMM", arLocale)

        while (true) {
            val now = Date()
            timeDigitsText = timeFormat.format(now)
            amPmText = if (amPmFormat.format(now).contains("ص")) "صباحاً" else "مساءً"
            currentDateText = dateFormat.format(now)
            delay(1000.milliseconds)
        }
    }

    val bgGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(colors = listOf(Color(0xFF0B0F19), Color(0xFF151D2A)))
        } else {
            Brush.verticalGradient(colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bar with Compact Adaptive Clock & Settings Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(
                            onClick = { onNavigate(CarScreen.Settings) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الإعدادات والصلاحيات",
                                tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "مشغل الوسائط للسيارة",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1
                            )
                            Text(
                                text = "اللوحة الرئيسية والعدادات المباشرة 🏎️",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight,
                                maxLines = 1
                            )
                        }
                    }

                    // Compact Adaptive Digital Automotive Clock Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF131C2E) else Color(0xFFE2E8F0),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) AutomotiveCyanAccent.copy(alpha = 0.7f) else AutomotiveBluePrimaryLight.copy(alpha = 0.4f)
                        ),
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "الوقت",
                                    tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                    modifier = Modifier.size(16.dp)
                                )

                                // Digital Clock Time Digits (hh:mm format)
                                Text(
                                    text = timeDigitsText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight
                                )

                                // Compact AM/PM Badge
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) AutomotiveCyanAccent.copy(alpha = 0.2f) else AutomotiveBluePrimaryLight.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = amPmText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            // Sub-Row Compact Date
                            Text(
                                text = currentDateText.ifEmpty { "التاريخ الحالي" },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 🏎️ 100% TRUTHFUL AUTOMOTIVE REAL TELEMETRY CARD WITH DISMISS (✕) BUTTON
                if (isTelemetryVisible) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) Color(0xFF111827) else Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, AutomotiveCyanAccent.copy(alpha = 0.6f)),
                        shadowElevation = 4.dp
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Dismiss (✕) Button in Top Left Corner
                            IconButton(
                                onClick = onDismissTelemetry,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إخفاء العدادات",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Title Header
                                Text(
                                    text = "عدادات وقياسات المركبة المباشرة 📊",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = 4.dp)
                                )

                                // Row 1: Speed + Engine RPM
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Vehicle Speed Gauge
                                    GaugeItem(
                                        title = "السرعة المباشرة",
                                        valueText = if (telemetry.speedKmh != null) "${telemetry.speedKmh} كم/س" else "غير مدعوم",
                                        icon = Icons.Default.Speed,
                                        accentColor = AutomotiveCyanAccent,
                                        isDark = isDark,
                                        modifier = Modifier.weight(1f)
                                    )



                                    // 2. Engine RPM Gauge
                                    GaugeItem(
                                        title = "دوران المحرك (RPM)",
                                        valueText = if (telemetry.rpm != null && telemetry.rpm > 0) "${telemetry.rpm} RPM" else "غير مدعوم",
                                        icon = Icons.Default.Sync,
                                        accentColor = Color(0xFF38BDF8),
                                        isDark = isDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Row 2: Coolant Temp + Battery Voltage
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 3. Engine Temperature Gauge
                                    GaugeItem(
                                        title = "حرارة المحرك",
                                        valueText = if (telemetry.engineTempC != null && telemetry.engineTempC > -40) "${telemetry.engineTempC}°C" else "غير مدعوم",
                                        icon = Icons.Default.Thermostat,
                                        accentColor = Color(0xFFFFB300),
                                        isDark = isDark,
                                        modifier = Modifier.weight(1f)
                                    )


                                    // 4. Battery Voltage Gauge
                                    GaugeItem(
                                        title = "جهد البطارية",
                                        valueText = if (telemetry.batteryVoltage != null && telemetry.batteryVoltage > 0f) "${String.format(Locale.US, "%.1f", telemetry.batteryVoltage)}V" else "غير مدعوم",
                                        icon = Icons.Default.ElectricBolt,
                                        accentColor = Color(0xFF10B981),
                                        isDark = isDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Sleek Travel Prayer Banner (دعاء المركوب)
                AnimatedVisibility(
                    visible = isPrayerBannerVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color(0xFF0F2027) else Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f)),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🤲",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "دعاء المركوب: \"سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ\"",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { isPrayerBannerVisible = false },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Primary Clean Dashboard Cards Grid (Audio, Video, Favorites, Settings)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 170.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Card 1: الصوتيات (Audio Player Card)
                    item {
                        PrimaryDashboardCard(
                            title = "الصوتيات والموسيقى",
                            subtitle = "$audioTracksCount مسارات صوتية",
                            icon = Icons.Default.MusicNote,
                            accentColor = AutomotiveCyanAccent,
                            isDark = isDark,
                            onClick = { onNavigate(CarScreen.Audio) }
                        )
                    }

                    // Card 2: المرئيات (Video Player Card)
                    item {
                        PrimaryDashboardCard(
                            title = "الصور والمرئيات",
                            subtitle = "$videoTracksCount مقطع فيديو",
                            icon = Icons.Default.Movie,
                            accentColor = Color(0xFFFF5252),
                            isDark = isDark,
                            onClick = { onNavigate(CarScreen.Video) }
                        )
                    }

                    // Card 3: المفضلة (Favorites Card)
                    item {
                        PrimaryDashboardCard(
                            title = "المفضلة وقوائم التشغيل",
                            subtitle = "$favoritesCount مفضلات مخصصة",
                            icon = Icons.Default.Favorite,
                            accentColor = Color(0xFFFF4081),
                            isDark = isDark,
                            onClick = { onNavigate(CarScreen.Favorites) }
                        )
                    }

                    // Card 4: الإعدادات والصلاحيات (Settings & Permissions Card)
                    item {
                        PrimaryDashboardCard(
                            title = "الإعدادات والصلاحيات",
                            subtitle = "التشغيل التلقائي والترخيص",
                            icon = Icons.Default.Settings,
                            accentColor = Color(0xFFFFB300),
                            isDark = isDark,
                            onClick = { onNavigate(CarScreen.Settings) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GaugeItem(
    title: String,
    valueText: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = accentColor.copy(alpha = 0.18f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (valueText.contains("غير مدعوم")) 12.sp else 15.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = if (valueText.contains("غير مدعوم")) MaterialTheme.colorScheme.outline.copy(alpha = 0.7f) else accentColor
            )
        }
    }
}

@Composable
fun PrimaryDashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDark) accentColor else AutomotiveBluePrimaryLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                )
            }
        }
    }
}
