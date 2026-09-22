package com.example.my_car.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.ui.components.MediaThumbnailImage
import com.example.my_car.ui.theme.*

@Composable
fun FavoritesScreen(
    tracks: List<MediaTrack>,
    favoriteVersion: Int = 0,
    onBack: () -> Unit,
    onTrackSelect: (MediaTrack) -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = الكل ⭐, 1 = الصوتيات 🎵, 2 = الفيديوهات 🎬, 3 = الصور 🖼️

    var selectedVideo by remember { mutableStateOf<MediaTrack?>(null) }
    var selectedPhoto by remember { mutableStateOf<MediaTrack?>(null) }

    // ⚡ Reactive Favorites Lists
    val favoriteTracks = remember(tracks, favoriteVersion) {
        tracks.filter { it.isFavorite }
    }

    val favoriteAudio = remember(favoriteTracks) { favoriteTracks.filter { !it.isVideo && !it.isImage } }
    val favoriteVideos = remember(favoriteTracks) { favoriteTracks.filter { it.isVideo } }
    val favoritePhotos = remember(favoriteTracks) { favoriteTracks.filter { it.isImage } }

    val displayedList = remember(favoriteTracks, selectedFilter) {
        when (selectedFilter) {
            1 -> favoriteAudio
            2 -> favoriteVideos
            3 -> favoritePhotos
            else -> favoriteTracks
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
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar
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
                        text = "المفضلة وقوائم التشغيل ⭐",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Filter Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            label = { Text("الكل ⭐ (${favoriteTracks.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            label = { Text("الصوتيات 🎵 (${favoriteAudio.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 2,
                            onClick = { selectedFilter = 2 },
                            label = { Text("الفيديوهات 🎬 (${favoriteVideos.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 3,
                            onClick = { selectedFilter = 3 },
                            label = { Text("الصور 🖼️ (${favoritePhotos.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Text(
                    text = "العناصر المضافة في المفضلة (${displayedList.size}):",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (displayedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد عناصر مضافة في هذه القائمة حالياً.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = displayedList,
                            key = { _, item -> "${item.id}_${item.isVideo}_${item.isImage}_${item.isFavorite}" }
                        ) { index, item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (item.isVideo) {
                                            selectedVideo = item
                                        } else if (item.isImage) {
                                            selectedPhoto = item
                                        } else {
                                            onTrackSelect(item)
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    Color(0xFFFF4081).copy(alpha = 0.5f)
                                )
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
                                            shape = CircleShape,
                                            color = Color(0xFFFF4081).copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = when {
                                                        item.isVideo -> Icons.Default.Movie
                                                        item.isImage -> Icons.Default.Image
                                                        else -> Icons.Default.MusicNote
                                                    },
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF4081),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${if (item.isVideo) "فيديو 🎬" else if (item.isImage) "صورة 🖼️" else "صوتية 🎵"} • ${item.folderName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { onToggleFavorite(item) }) {
                                            Icon(
                                                imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "إلغاء التفضيل",
                                                tint = if (item.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        if (!item.isImage) {
                                            Text(
                                                text = item.formatDuration(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFFF4081),
                                                modifier = Modifier.padding(start = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Video Player Overlay
            if (selectedVideo != null) {
                MxVideoPlayerView(
                    video = selectedVideo!!,
                    onClosePlayer = { selectedVideo = null }
                )
            }

            // Fullscreen Photo Viewer Overlay
            if (selectedPhoto != null) {
                FullscreenPhotoViewer(
                    photo = selectedPhoto!!,
                    onToggleFavorite = onToggleFavorite,
                    onCloseViewer = { selectedPhoto = null }
                )
            }
        }
    }
}
