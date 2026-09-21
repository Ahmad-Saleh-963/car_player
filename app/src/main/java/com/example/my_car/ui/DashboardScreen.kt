package com.example.my_car.ui

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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
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
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CarScreen {
    object Dashboard : CarScreen()
    object Audio : CarScreen()
    object Video : CarScreen()
    object Folders : CarScreen()
    object Favorites : CarScreen()
}

@Composable
fun DashboardScreen(
    currentTrack: MediaTrack?,
    isPlaying: Boolean,
    audioTracksCount: Int,
    videoTracksCount: Int,
    favoritesCount: Int,
    onNavigate: (CarScreen) -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var isPrayerBannerVisible by remember { mutableStateOf(true) }

    // Live Clock State
    var timeDigitsText by remember { mutableStateOf("12:00") }
    var amPmText by remember { mutableStateOf("صباحاً") }
    var currentDateText by remember { mutableStateOf("") }

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
            delay(1000)
        }
    }

    val bgGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF0B0F19), Color(0xFF151D2A)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar with Compact Adaptive Clock
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
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                            modifier = Modifier.size(28.dp)
                        )
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
                                text = "اللوحة الرئيسية للتحكم والملاحة",
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

                // Primary Clean Dashboard Cards Grid (Audio, Video, Favorites)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
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
                            title = "المرئيات والفيديوهات",
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
