@file:OptIn(UnstableApi::class)

package com.example.my_car.ui

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.data.repository.AudioFolder
import com.example.my_car.ui.components.MediaThumbnailImage
import com.example.my_car.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun VideoPlayerScreen(
    videos: List<MediaTrack>,
    photos: List<MediaTrack> = emptyList(),
    onBack: () -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedVideo by remember { mutableStateOf<MediaTrack?>(null) }
    var selectedPhoto by remember { mutableStateOf<MediaTrack?>(null) }

    if (selectedVideo != null) {
        MxVideoPlayerView(
            video = selectedVideo!!,
            onClosePlayer = { selectedVideo = null }
        )
    } else if (selectedPhoto != null) {
        FullscreenPhotoViewer(
            photo = selectedPhoto!!,
            onToggleFavorite = onToggleFavorite,
            onCloseViewer = { selectedPhoto = null }
        )
    } else {
        VisualsGalleryView(
            videos = videos,
            photos = photos,
            onBack = onBack,
            onVideoSelect = { selectedVideo = it },
            onPhotoSelect = { selectedPhoto = it },
            onToggleFavorite = onToggleFavorite,
            modifier = modifier
        )
    }
}

@Composable
fun VisualsGalleryView(
    videos: List<MediaTrack>,
    photos: List<MediaTrack>,
    onBack: () -> Unit,
    onVideoSelect: (MediaTrack) -> Unit,
    onPhotoSelect: (MediaTrack) -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = الفيديوهات 🎬, 1 = الصور 🖼️, 2 = المجلدات 📁, 3 = المفضلة ⭐

    // Expanded Folder State for Tab 2
    var expandedFolder by remember { mutableStateOf<String?>(null) }

    // Grouping Folders (Combines Videos & Photos into folders)
    val allVisuals = remember(videos, photos) { videos + photos }

    val folderGroups = remember(allVisuals) {
        allVisuals.groupBy { it.folderName }
            .map { (folderName, folderItems) -> AudioFolder(folderName, folderItems) }
            .sortedBy { it.name }
    }

    val favorites = remember(allVisuals) {
        allVisuals.filter { it.isFavorite }
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
                        text = "معرض المرئيات والصور 🎬🖼️",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Filter Chips Row (Same arrangement as Audio Player)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            label = { Text("الفيديوهات 🎬 (${videos.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            label = { Text("الصور 🖼️ (${photos.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 2,
                            onClick = { selectedFilter = 2 },
                            label = { Text("المجلدات 📁 (${folderGroups.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 3,
                            onClick = { selectedFilter = 3 },
                            label = { Text("المفضلة ⭐ (${favorites.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Content View Based on Selected Tab
                when (selectedFilter) {
                    0 -> {
                        // 🎬 0: VIDEOS GRID
                        if (videos.isEmpty()) {
                            EmptyStateView("لم يتم العثور على مقاطع فيديو في ذاكرة الجهاز.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 180.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = videos,
                                    key = { "v_${it.id}_${it.isFavorite}" }
                                ) { video ->
                                    VideoGridCardItem(
                                        video = video,
                                        isDark = isDark,
                                        onVideoSelect = onVideoSelect,
                                        onToggleFavorite = onToggleFavorite
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // 🖼️ 1: PHOTOS GRID
                        if (photos.isEmpty()) {
                            EmptyStateView("لم يتم العثور على صور أو لقطات محلياً في الذاكرة.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = photos,
                                    key = { "p_${it.id}_${it.isFavorite}" }
                                ) { photo ->
                                    PhotoGridCardItem(
                                        photo = photo,
                                        isDark = isDark,
                                        onPhotoSelect = onPhotoSelect,
                                        onToggleFavorite = onToggleFavorite
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // 📁 2: FOLDERS VIEW
                        if (folderGroups.isEmpty()) {
                            EmptyStateView("لا توجد مجلدات مرئية حالياً.")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                folderGroups.forEach { folder ->
                                    val isExpanded = expandedFolder == folder.name

                                    // Folder Header
                                    item(key = "f_header_${folder.name}") {
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    expandedFolder = if (isExpanded) null else folder.name
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFB300),
                                                        modifier = Modifier.size(26.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = folder.name,
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${folder.trackCount} عنصر مرئي (فيديو وصور)",
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
                                        }
                                    }

                                    // Folder Contents Items
                                    if (isExpanded) {
                                        items(
                                            items = folder.tracks,
                                            key = { "f_item_${folder.name}_${it.id}_${it.isFavorite}" }
                                        ) { item ->
                                            if (item.isImage) {
                                                PhotoGridCardItem(
                                                    photo = item,
                                                    isDark = isDark,
                                                    onPhotoSelect = onPhotoSelect,
                                                    onToggleFavorite = onToggleFavorite,
                                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                                                )
                                            } else {
                                                VideoGridCardItem(
                                                    video = item,
                                                    isDark = isDark,
                                                    onVideoSelect = onVideoSelect,
                                                    onToggleFavorite = onToggleFavorite,
                                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // ⭐ 3: FAVORITES VIEW
                        if (favorites.isEmpty()) {
                            EmptyStateView("لا توجد صور أو فيديوهات مضافة في المفضلة.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 160.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = favorites,
                                    key = { "fav_${it.id}_${it.isFavorite}" }
                                ) { item ->
                                    if (item.isImage) {
                                        PhotoGridCardItem(
                                            photo = item,
                                            isDark = isDark,
                                            onPhotoSelect = onPhotoSelect,
                                            onToggleFavorite = onToggleFavorite
                                        )
                                    } else {
                                        VideoGridCardItem(
                                            video = item,
                                            isDark = isDark,
                                            onVideoSelect = onVideoSelect,
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
    }
}

@Composable
fun VideoGridCardItem(
    video: MediaTrack,
    isDark: Boolean,
    onVideoSelect: (MediaTrack) -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onVideoSelect(video) }
            .border(
                1.dp,
                Color(0xFFFF5252).copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFF5252).copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    MediaThumbnailImage(
                        uri = video.uri,
                        title = video.title,
                        isVideo = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleFavorite(video) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "مفضل",
                            tint = if (video.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = video.formatDuration(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFF5252)
                    )
                }
            }

            Column {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "📁 ${video.folderName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                )
            }
        }
    }
}

@Composable
fun PhotoGridCardItem(
    photo: MediaTrack,
    isDark: Boolean,
    onPhotoSelect: (MediaTrack) -> Unit,
    onToggleFavorite: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPhotoSelect(photo) }
            .border(
                1.dp,
                AutomotiveCyanAccent.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.uri)
                    .crossfade(true)
                    .size(256)
                    .build(),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay for Text Visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 60f
                        )
                    )
            )

            // Favorite Button Top End
            IconButton(
                onClick = { onToggleFavorite(photo) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "مفضل",
                    tint = if (photo.isFavorite) Color(0xFFFF4081) else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Photo Title & Folder Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = photo.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "🖼️ ${photo.folderName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

// 🖼️ FULLSCREEN PHOTO VIEWER WITH ZOOM & PAN
@Composable
fun FullscreenPhotoViewer(
    photo: MediaTrack,
    onToggleFavorite: (MediaTrack) -> Unit,
    onCloseViewer: () -> Unit
) {
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Fullscreen Photo
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photo.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = photo.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onCloseViewer() }
            )

            // Header Control Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onCloseViewer,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "خروج",
                            tint = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "📁 ${photo.folderName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(photo) },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "مفضل",
                        tint = if (photo.isFavorite) Color(0xFFFF4081) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MxVideoPlayerView(
    video: MediaTrack,
    onClosePlayer: () -> Unit
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(video.durationMs) }
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var hasError by remember { mutableStateOf(false) }

    val exoPlayer = remember(context, video) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.uri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = exoPlayer.duration.coerceAtLeast(1L)
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                hasError = true
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Position Tracker Loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            delay(500)
        }
    }

    // Auto Hide Controls Delay
    LaunchedEffect(showControls) {
        if (showControls && !isLocked) {
            delay(4000)
            showControls = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable {
                    if (!isLocked) {
                        showControls = !showControls
                    } else {
                        showControls = true // reveal lock button only
                    }
                }
        ) {
            // Player Surface
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Custom MX Controls
                        this.resizeMode = resizeMode
                    }
                },
                update = { playerView ->
                    playerView.resizeMode = resizeMode
                },
                modifier = Modifier.fillMaxSize()
            )

            // Error Dialog
            if (hasError) {
                AlertDialog(
                    onDismissRequest = onClosePlayer,
                    title = { Text("خطأ في تشغيل الفيديو") },
                    text = { Text("الملف المرئي تالف أو غير مدعوم من نظام التشغيل.") },
                    confirmButton = {
                        Button(onClick = onClosePlayer) {
                            Text("العودة للقائمة")
                        }
                    }
                )
            }

            // MX Player Style Controls Overlay
            AnimatedVisibility(
                visible = showControls,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(16.dp)
                ) {
                    if (isLocked) {
                        // Lock Screen Icon Button Only
                        IconButton(
                            onClick = { isLocked = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(48.dp)
                                .background(Color(0xFFFF5252), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "إلغاء القفل",
                                tint = Color.White
                            )
                        }
                    } else {
                        // Top Bar: Back + Title + Scale Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = onClosePlayer,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF263345), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "خروج",
                                        tint = Color.White
                                    )
                                }

                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Aspect Ratio Toggle Icon Button
                                IconButton(
                                    onClick = {
                                        resizeMode = when (resizeMode) {
                                            AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                            AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                            else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AspectRatio,
                                        contentDescription = "أبعاد الشاشة",
                                        tint = Color.White
                                    )
                                }

                                // Lock Screen Icon Button
                                IconButton(
                                    onClick = {
                                        isLocked = true
                                        showControls = false
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "قفل",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Center Transport Controls
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    exoPlayer.seekTo(
                                        (exoPlayer.currentPosition - 10000).coerceAtLeast(
                                            0
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "تأخير 10ث",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (exoPlayer.isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                },
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFF00E5FF), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    exoPlayer.seekTo(
                                        (exoPlayer.currentPosition + 10000).coerceAtMost(
                                            durationMs
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "تقديم 10ث",
                                    tint = Color.White
                                )
                            }
                        }

                        // Bottom Seekbar & Speed Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Slider(
                                value = currentPositionMs.toFloat(),
                                onValueChange = { newPos ->
                                    currentPositionMs = newPos.toLong()
                                    exoPlayer.seekTo(newPos.toLong())
                                },
                                valueRange = 0f..durationMs.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00E5FF),
                                    activeTrackColor = Color(0xFF00E5FF)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${formatMs(currentPositionMs)} / ${formatMs(durationMs)}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )

                                // Speed Options
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (playbackSpeed == speed) Color(0xFF00E5FF) else Color.White.copy(
                                                alpha = 0.2f
                                            ),
                                            modifier = Modifier.clickable {
                                                playbackSpeed = speed
                                                exoPlayer.setPlaybackSpeed(speed)
                                            }
                                        ) {
                                            Text(
                                                text = "${speed}x",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (playbackSpeed == speed) Color(
                                                    0xFF0F172A
                                                ) else Color.White,
                                                modifier = Modifier.padding(
                                                    horizontal = 8.dp,
                                                    vertical = 4.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
