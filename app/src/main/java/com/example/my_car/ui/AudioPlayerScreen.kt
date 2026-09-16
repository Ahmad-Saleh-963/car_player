package com.example.my_car.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.ui.components.AutomotiveHardwareControlBar
import com.example.my_car.ui.components.MediaThumbnailImage
import com.example.my_car.ui.theme.*
import java.util.Locale

@Composable
fun AudioPlayerScreen(
    tracks: List<MediaTrack>,
    currentTrack: MediaTrack?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    isRepeatOne: Boolean,
    onBack: () -> Unit,
    onTrackSelect: (MediaTrack) -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = المجلدات 📁 (الافتراضي), 1 = الكل, 2 = الفنانون, 3 = المفضلة
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    var expandedArtists by remember { mutableStateOf(setOf<String>()) }

    val filteredTracks = remember(tracks, searchQuery, selectedFilter) {
        var result = if (searchQuery.isBlank()) tracks else {
            tracks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.folderName.contains(searchQuery, ignoreCase = true)
            }
        }
        if (selectedFilter == 3) {
            result = result.filter { it.isFavorite }
        }
        result
    }

    val folderGroups = remember(filteredTracks) {
        filteredTracks.groupBy { it.folderName }
    }

    val artistGroups = remember(filteredTracks) {
        filteredTracks.groupBy { it.artist }
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
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isDark) Color(0xFF263345) else Color(0xFFCBD5E1),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الرجوع",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) searchQuery = ""
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "البحث",
                                tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = "مشغل الصوتيات المتكامل 🎧",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Expandable Search Bar
                AnimatedVisibility(visible = isSearchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث عن أغنية...") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // SIDE-BY-SIDE Top Controls Row (100% Equal Height Matching via IntrinsicSize.Max)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Ultra-Compact Hero Player Card (Weight 1.35f)
                    Surface(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                            .border(
                                1.dp,
                                if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                RoundedCornerShape(14.dp)
                            ),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isDark) AutomotiveCyanAccent.copy(alpha = 0.2f) else AutomotiveBluePrimaryLight.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        if (currentTrack != null) {
                                            MediaThumbnailImage(
                                                uri = currentTrack.uri,
                                                title = currentTrack.title,
                                                isVideo = false,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.MusicNote,
                                                    contentDescription = null,
                                                    tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = currentTrack?.title ?: "اختر مساراً",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${currentTrack?.artist ?: "فنان مجهول"} • ${if (isShuffle) "عشوائي 🔀" else "متتالي 🔁"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (currentTrack != null) {
                                    IconButton(
                                        onClick = { onToggleFavorite(currentTrack) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentTrack.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "مفضل",
                                            tint = if (currentTrack.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                AnimatedAudioGlow(isPlaying = isPlaying, isDark = isDark)
                            }

                            // Compact RTL Slider
                            if (currentTrack != null) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Slider(
                                        value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                                        onValueChange = { onSeekTo(it.toLong()) },
                                        valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                                        colors = SliderDefaults.colors(
                                            thumbColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                            activeTrackColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight
                                        ),
                                        modifier = Modifier.height(18.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = formatAudioMs(currentPositionMs),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatAudioMs(durationMs),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                                        )
                                    }
                                }
                            }

                            // Compact Transport Bar Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onToggleShuffle, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "عشوائي",
                                        tint = if (isShuffle) (if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(onClick = { onSeekTo((currentPositionMs - 10000).coerceAtLeast(0)) }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.FastRewind, contentDescription = "-10s", modifier = Modifier.size(18.dp))
                                }

                                IconButton(onClick = onPrevClick, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "السابق", modifier = Modifier.size(20.dp))
                                }

                                FilledIconButton(
                                    onClick = onPlayPauseClick,
                                    enabled = currentTrack != null,
                                    modifier = Modifier.size(38.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                        contentColor = if (isDark) Color(0xFF0F172A) else Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                IconButton(onClick = onNextClick, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "التالي", modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = { onSeekTo((currentPositionMs + 10000).coerceAtMost(durationMs)) }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.FastForward, contentDescription = "+10s", modifier = Modifier.size(18.dp))
                                }

                                IconButton(onClick = onToggleRepeat, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (isRepeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                        contentDescription = "تكرار",
                                        tint = if (isRepeatOne) (if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight) else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Hardware Controls Card (100% Height Matched)
                    AutomotiveHardwareControlBar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        isVerticalLayout = true
                    )
                }

                // Header Filter Chips Row (المجلدات 📁 (الافتراضي) -> الكل 🎵 -> الفنانون 👤 -> المفضلة ⭐)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            label = { Text("حسب المجلدات 📁 (${folderGroups.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            label = { Text("جميع الأغاني 🎵 (${tracks.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedFilter == 2,
                            onClick = { selectedFilter = 2 },
                            label = { Text("الفنانون 👤 (${artistGroups.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedFilter == 3,
                            onClick = { selectedFilter = 3 },
                            label = { Text("المفضلة ⭐ (${tracks.count { it.isFavorite }})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Dynamic List Display based on selectedFilter
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (selectedFilter) {
                        0 -> {
                            // 0: حسب المجلدات 📁 (Direct Tree Expansion)
                            folderGroups.forEach { (folderName, fTracks) ->
                                item(key = "folder_$folderName") {
                                    val isExpanded = folderName in expandedFolders

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            // Folder Header Row
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedFolders = if (isExpanded) {
                                                            expandedFolders - folderName
                                                        } else {
                                                            expandedFolders + folderName
                                                        }
                                                    }
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = folderName,
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${fTracks.size} مسارات صوتية",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFFFFB300)
                                                        )
                                                    }
                                                }

                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "توسيع",
                                                    tint = Color(0xFFFFB300)
                                                )
                                            }

                                            // Expanded Inner Songs Directly Under This Folder
                                            AnimatedVisibility(visible = isExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    fTracks.forEach { track ->
                                                        AudioTrackRowItem(
                                                            track = track,
                                                            isSelected = currentTrack?.id == track.id,
                                                            isDark = isDark,
                                                            onTrackSelect = onTrackSelect,
                                                            onToggleFavorite = onToggleFavorite
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // 2: حسب الفنانين 👤 (Expandable Artists)
                            artistGroups.forEach { (artistName, aTracks) ->
                                item(key = "artist_$artistName") {
                                    val isExpanded = artistName in expandedArtists

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, AutomotiveCyanAccent.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expandedArtists = if (isExpanded) {
                                                            expandedArtists - artistName
                                                        } else {
                                                            expandedArtists + artistName
                                                        }
                                                    }
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = AutomotiveCyanAccent,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = artistName,
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${aTracks.size} أغاني",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = AutomotiveCyanAccent
                                                        )
                                                    }
                                                }

                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "توسيع",
                                                    tint = AutomotiveCyanAccent
                                                )
                                            }

                                            AnimatedVisibility(visible = isExpanded) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    aTracks.forEach { track ->
                                                        AudioTrackRowItem(
                                                            track = track,
                                                            isSelected = currentTrack?.id == track.id,
                                                            isDark = isDark,
                                                            onTrackSelect = onTrackSelect,
                                                            onToggleFavorite = onToggleFavorite
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // 1: جميع الأغاني 🎵  أو  3: المفضلة ⭐
                            itemsIndexed(filteredTracks, key = { _, t -> t.id }) { _, track ->
                                AudioTrackRowItem(
                                    track = track,
                                    isSelected = currentTrack?.id == track.id,
                                    isDark = isDark,
                                    onTrackSelect = onTrackSelect,
                                    onToggleFavorite = onToggleFavorite
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioTrackRowItem(
    track: MediaTrack,
    isSelected: Boolean,
    isDark: Boolean,
    onTrackSelect: (MediaTrack) -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTrackSelect(track) },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            if (isDark) Color(0xFF0284C7).copy(alpha = 0.25f) else Color(0xFF38BDF8).copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) AutomotiveCyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) AutomotiveCyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    modifier = Modifier.size(34.dp)
                ) {
                    MediaThumbnailImage(
                        uri = track.uri,
                        title = track.title,
                        isVideo = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${track.artist} • ${track.folderName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onToggleFavorite(track) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مفضل",
                        tint = if (track.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = track.formatDuration(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

// Audio Synchronized Visualizer Glow
@Composable
fun AnimatedAudioGlow(isPlaying: Boolean, isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioGlow")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.2f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleGlow"
    )

    Surface(
        shape = CircleShape,
        color = if (isPlaying) {
            if (isDark) AutomotiveCyanAccent.copy(alpha = 0.25f) else AutomotiveBluePrimaryLight.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        },
        modifier = Modifier
            .size(36.dp)
            .scale(if (isPlaying) scale else 1.0f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun formatAudioMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
