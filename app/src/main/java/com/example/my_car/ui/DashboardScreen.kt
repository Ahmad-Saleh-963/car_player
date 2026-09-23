package com.example.my_car.ui

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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

// 🚗 HELPER TO DETECT CAR HEAD UNITS (Fly Golden, TS10, FYT, Automotive OS)
fun isCarHeadUnitDevice(context: Context): Boolean {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    if (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_CAR) {
        return true
    }
    val config = context.resources.configuration
    val isCarUi = (config.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_CAR
    val hasAutomotiveFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    return isCarUi || hasAutomotiveFeature
}

@Composable
fun DashboardScreen(
    audioTracksCount: Int,
    videoTracksCount: Int,
    favoritesCount: Int,
    isTelemetryVisible: Boolean = true,
    showTelemetryOnAllDevices: Boolean = false,
    onDismissTelemetry: () -> Unit = {},
    onNavigate: (CarScreen) -> Unit,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var isPrayerBannerVisible by remember { mutableStateOf(true) }
    var isEmergencyAlertDismissed by remember { mutableStateOf(false) }

    // Check if running on a Car Head Unit or if forced in settings
    val isCarUnit = remember(context) { isCarHeadUnitDevice(context) }
    val shouldShowGauges = isTelemetryVisible && (isCarUnit || showTelemetryOnAllDevices)

    // Live Clock State
    var timeDigitsText by remember { mutableStateOf("12:00") }
    var amPmText by remember { mutableStateOf("صباحاً") }
    var currentDateText by remember { mutableStateOf("") }

    // Live Real Vehicle Telemetry State
    val telemetry = VehicleTelemetryManager.telemetry

    // 🚨 ACCURATE AUTOMOTIVE SAFETY DANGER & WARNING THRESHOLDS
    val isTempDanger = telemetry.engineTempC != null && telemetry.engineTempC >= 100
    val isTempWarning = telemetry.engineTempC != null && telemetry.engineTempC in 95..99

    val isRpmDanger = telemetry.rpm != null && telemetry.rpm >= 5000
    val isRpmWarning = telemetry.rpm != null && telemetry.rpm in 4000..4999

    // 12V Car Battery Danger (< 11.2V or > 15.5V). Phone lithium batteries (3.7V - 4.3V) do NOT trigger car battery alert!
    val isVoltDanger = telemetry.batteryVoltage != null && telemetry.batteryVoltage >= 8.0f && (telemetry.batteryVoltage !in 11.2f..15.5f)
    val isVoltWarning = telemetry.batteryVoltage != null && telemetry.batteryVoltage >= 8.0f && (telemetry.batteryVoltage in 11.2f..11.8f || telemetry.batteryVoltage in 14.8f..15.4f)

    val isSpeedDanger = telemetry.speedKmh != null && telemetry.speedKmh >= 140
    val isSpeedWarning = telemetry.speedKmh != null && telemetry.speedKmh in 120..139

    val hasAnyDanger = isTempDanger || isRpmDanger || isVoltDanger || isSpeedDanger

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
                                text = if (isCarUnit) "نظام الشاشة المجهزة للسيارة 🏎️" else "اللوحة الرئيسية والمشغل",
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

                                Text(
                                    text = timeDigitsText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight
                                )

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

                            Text(
                                text = currentDateText.ifEmpty { "التاريخ الحالي" },
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight,
                                maxLines = 1
                            )
                        }
                    }
                }

                // 🚨 SEPARATE DISMISSABLE EMERGENCY SAFETY BANNER (Only shows when in active danger!)
                if (hasAnyDanger && !isEmergencyAlertDismissed) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF2D0A0F),
                        border = BorderStroke(1.5.dp, Color(0xFFFF1744)),
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFF1744).copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "خطر",
                                            tint = Color(0xFFFF1744),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "🚨 تنبيه أمان المركبة المباشر",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFFF1744)
                                    )
                                    Text(
                                        text = when {
                                            isTempDanger -> "⚠️ حرارة سائل التبريد مرتفعة جداً (${telemetry.engineTempC}°C). يرجى التوقف الآمن وفحص مياه الرادياتير والمروحة!"
                                            isVoltDanger -> "⚠️ جهد بطارية السيارة منخفض (${String.format(Locale.US, "%.1f", telemetry.batteryVoltage)}V). يرجى فحص الدينامو والكهرباء لمنع توقف المحرك!"
                                            isRpmDanger -> "⚠️ دوران المحرك مرتفع جداً (${telemetry.rpm} RPM). يرجى التبديل لغيار أعلى لتخفيف الضغط!"
                                            else -> "⚠️ تم إكتشاف حالة تجاوز في سرعة السيارة (${telemetry.speedKmh} كم/س)!"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = { isEmergencyAlertDismissed = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق التنبيه",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 🏎️ AUTOMOTIVE GAUGES CARD (SHOWS ON CAR HEAD UNITS OR IF ENABLED IN SETTINGS)
                if (shouldShowGauges) {
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

                                // Row 1: Speed + Engine RPM (Tachometer in x1000 RPM)
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
                                        isDanger = isSpeedDanger,
                                        isWarning = isSpeedWarning,
                                        modifier = Modifier.weight(1f)
                                    )


                                    // 2. Engine RPM Gauge (x1000 Tachometer style)
                                    GaugeItem(
                                        title = "دوران المحرك (RPM)",
                                        valueText = formatCarRpm(telemetry.rpm),
                                        subBadge = if (telemetry.rpm != null && telemetry.rpm > 0) "x1000 RPM" else null,
                                        icon = Icons.Default.Sync,
                                        accentColor = Color(0xFF38BDF8),
                                        isDark = isDark,
                                        isDanger = isRpmDanger,
                                        isWarning = isRpmWarning,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Row 2: Coolant Temp (Car Gauge Level: C / ¼ / ½ / ¾ / H) + Battery Voltage
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 3. Engine Temperature Gauge (Dial Status)
                                    GaugeItem(
                                        title = "حرارة المحرك",
                                        valueText = formatCarTemp(telemetry.engineTempC),
                                        icon = Icons.Default.Thermostat,
                                        accentColor = Color(0xFFFFB300),
                                        isDark = isDark,
                                        isDanger = isTempDanger,
                                        isWarning = isTempWarning,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // 4. Battery Voltage Gauge
                                    GaugeItem(
                                        title = "جهد البطارية",
                                        valueText = formatCarVoltage(telemetry.batteryVoltage),
                                        icon = Icons.Default.ElectricBolt,
                                        accentColor = Color(0xFF10B981),
                                        isDark = isDark,
                                        isDanger = isVoltDanger,
                                        isWarning = isVoltWarning,
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

// 🚗 HELPER FORMATTERS FOR STANDARD AUTOMOTIVE GAUGES
fun formatCarRpm(rpm: Int?): String {
    if (rpm == null || rpm <= 0) return "غير مدعوم"
    val rpmInThousands = rpm / 1000f
    return String.format(Locale.US, "%.1f", rpmInThousands)
}

fun formatCarTemp(tempC: Int?): String {
    if (tempC == null || tempC < -40) return "غير مدعوم"
    return when {
        tempC < 60 -> "بارد C (${tempC}°C)"
        tempC in 60..79 -> "ربع ¼ (${tempC}°C)"
        tempC in 80..94 -> "نصف ½ (${tempC}°C)"
        tempC in 95..99 -> "مرتفع ¾ (${tempC}°C)"
        else -> "كامل H (${tempC}°C)"
    }
}

fun formatCarVoltage(voltage: Float?): String {
    if (voltage == null || voltage <= 0f) return "غير مدعوم"
    return if (voltage < 8.0f) {
        String.format(Locale.US, "%.1fV (هاتف)", voltage)
    } else {
        String.format(Locale.US, "%.1fV", voltage)
    }
}

@Composable
fun GaugeItem(
    title: String,
    valueText: String,
    icon: ImageVector,
    accentColor: Color,
    isDark: Boolean,
    subBadge: String? = null,
    isDanger: Boolean = false,
    isWarning: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val effectiveColor = when {
        isDanger -> Color(0xFFFF1744)
        isWarning -> Color(0xFFFF9100)
        else -> accentColor
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = effectiveColor.copy(alpha = if (isDanger) 0.3f else 0.18f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isDanger) Icons.Default.Warning else icon,
                    contentDescription = title,
                    tint = effectiveColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = if (isDanger) Color(0xFFFF1744) else (if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight)
                )

                if (isDanger) {
                    Text(
                        text = "⚠️ خطر!",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFFF1744)
                    )
                } else if (isWarning) {
                    Text(
                        text = "⚠️ تحذير",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFFF9100)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (valueText.contains("غير مدعوم")) 12.sp else 15.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (valueText.contains("غير مدعوم")) MaterialTheme.colorScheme.outline.copy(alpha = 0.7f) else effectiveColor
                )

                if (subBadge != null && !valueText.contains("غير مدعوم")) {
                    Text(
                        text = subBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = effectiveColor.copy(alpha = 0.8f)
                    )
                }
            }
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
