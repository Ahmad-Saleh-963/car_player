package com.example.my_car.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.ui.theme.*

@Composable
fun FoldersScreen(
    tracks: List<MediaTrack>,
    onBack: () -> Unit,
    onTrackSelect: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    val folderMap = tracks.groupBy { it.folderName }

    val bgGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF0B0F19), Color(0xFF151D2A)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isDark) Color(0xFF263345) else Color(0xFFCBD5E1),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الرجوع"
                    )
                }

                Text(
                    text = "مجلدات الجهاز الصوتية 📁",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "المجلدات المكتشفة في الذاكرة الحقيقية (${folderMap.size}):",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 220.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(folderMap.toList()) { (folderName, folderTracks) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .border(
                                1.dp,
                                Color(0xFFFFB300).copy(alpha = 0.5f),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                if (folderTracks.isNotEmpty()) onTrackSelect(folderTracks.first())
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = folderName,
                                            tint = Color(0xFFFFB300)
                                        )
                                    }
                                }

                                Text(
                                    text = "${folderTracks.size} مسارات",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFB300)
                                )
                            }

                            Text(
                                text = folderName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
