@file:OptIn(UnstableApi::class)

package com.example.my_car.ui

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.example.my_car.data.model.MediaTrack
import com.example.my_car.ui.components.MediaThumbnailImage
import com.example.my_car.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun VideoPlayerScreen(
    videos: List<MediaTrack>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedVideo by remember { mutableStateOf<MediaTrack?>(null) }

    if (selectedVideo != null) {
        MxVideoPlayerView(
            video = selectedVideo!!,
            onClosePlayer = { selectedVideo = null }
        )
    } else {
        VideoGalleryView(
            videos = videos,
            onBack = onBack,
            onVideoSelect = { selectedVideo = it },
            modifier = modifier
        )
    }
}

@Composable
fun VideoGalleryView(
    videos: List<MediaTrack>,
    onBack: () -> Unit,
    onVideoSelect: (MediaTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = "مشغل الفيديو الاحترافي (MX Style) 🎬",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "مقاطع الفيديو المكتشفة في الذاكرة (${videos.size}):",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (videos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم يتم العثور على مقاطع فيديو محلياً في ذاكرة الجهاز.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 200.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(videos) { video ->
                            Surface(
                                modifier = Modifier
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
                                        .padding(12.dp),
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
                                            modifier = Modifier.size(46.dp)
                                        ) {
                                            MediaThumbnailImage(
                                                uri = video.uri,
                                                title = video.title,
                                                isVideo = true,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = video.formatDuration(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFFF5252)
                                            )
                                            if (video.formatSize().isNotEmpty()) {
                                                Text(
                                                    text = video.formatSize(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isDark) AutomotiveTextSecondaryDark else AutomotiveTextSecondaryLight
                                                )
                                            }
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
                    }
                }
            }
        }
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
