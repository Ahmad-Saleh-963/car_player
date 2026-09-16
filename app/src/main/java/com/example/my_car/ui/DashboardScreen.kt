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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
    var timeDigitsText by remember { mutableStateOf("12:00:00") }
    var amPmText by remember { mutableStateOf("صباحاً") }
    var currentDateText by remember { mutableStateOf("") }

    // Ticking Clock Effect
    LaunchedEffect(Unit) {
        val arLocale = Locale.forLanguageTag("ar")
        val timeFormat = SimpleDateFormat("hh:mm:ss", arLocale)
        val amPmFormat = SimpleDateFormat("a", arLocale)
        val dateFormat = SimpleDateFormat("EEEE • d MMMM yyyy", arLocale)

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
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar with High-End Automotive Digital Clock & Quick Run Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "مشغل الوسائط للسيارة",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "اللوحة الرئيسية للتحكم والملاحة",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Run Execution Button
                        Button(
                            onClick = onPlayPauseClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlaying) Color(0xFFF59E0B) else Color(0xFF10B981),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Run",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isPlaying) "إيقاف ⏸️" else "تشغيل وتنفيذ 🚀 Run",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }

                        // High-End Digital Automotive Clock Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color(0xFF131C2E) else Color(0xFFE2E8F0),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) AutomotiveCyanAccent.copy(alpha = 0.8f) else AutomotiveBluePrimaryLight.copy(alpha = 0.5f)
                            ),
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Arabic AM/PM Pill Badge
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) AutomotiveCyanAccent.copy(alpha = 0.2f) else AutomotiveBluePrimaryLight.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = amPmText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Digital Clock Time Digits
                                    Text(
                                        text = timeDigitsText,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight
                                    )

                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "الوقت",
                                        tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Sub-Row Date
                                Text(
                                    text = currentDateText.ifEmpty { "التاريخ الحالي" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                                )
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
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Color(0xFF0F2027) else Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f)),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🤲",
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "دعاء المركوب: \"سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { isPrayerBannerVisible = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Primary Clean Dashboard Cards Grid (Audio, Video, Favorites)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDark) accentColor else AutomotiveBluePrimaryLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                )
            }
        }
    }
}
