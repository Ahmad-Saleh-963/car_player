package com.example.my_car.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Global High-Performance LRU Memory Cache for Video Thumbnails (Instant 0ms lookup)
private val videoThumbnailMemoryCache = LruCache<Uri, Bitmap>(64)

@Composable
fun MediaThumbnailImage(
    uri: Uri,
    title: String,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    isListItem: Boolean = false
) {
    val context = LocalContext.current
    var thumbnailBitmap by remember(uri) { mutableStateOf<Bitmap?>(videoThumbnailMemoryCache.get(uri)) }
    var hasError by remember(uri) { mutableStateOf(false) }

    val placeholderGradient = remember(isVideo, title) {
        if (isVideo) {
            Brush.linearGradient(colors = listOf(Color(0xFF881337), Color(0xFFE11D48)))
        } else {
            val hue = (title.hashCode() and 0x7FFFFFFF) % 360
            val colorStart = Color.hsl(hue.toFloat(), 0.65f, 0.40f)
            val colorEnd = Color.hsl((hue + 40) % 360f, 0.70f, 0.20f)
            Brush.linearGradient(colors = listOf(colorStart, colorEnd))
        }
    }

    if (isVideo && thumbnailBitmap == null) {
        LaunchedEffect(uri) {
            withContext(Dispatchers.IO) {
                try {
                    val cached = videoThumbnailMemoryCache.get(uri)
                    if (cached != null) {
                        thumbnailBitmap = cached
                        return@withContext
                    }

                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        context.contentResolver.loadThumbnail(uri, Size(128, 128), null)
                    } else {
                        val id = uri.lastPathSegment?.toLongOrNull()
                        if (id != null) {
                            @Suppress("DEPRECATION")
                            MediaStore.Video.Thumbnails.getThumbnail(
                                context.contentResolver,
                                id,
                                MediaStore.Video.Thumbnails.MICRO_KIND,
                                null
                            )
                        } else null
                    }
                    if (bitmap != null) {
                        videoThumbnailMemoryCache.put(uri, bitmap)
                        thumbnailBitmap = bitmap
                    } else {
                        hasError = true
                    }
                } catch (e: Exception) {
                    hasError = true
                }
            }
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        if (isVideo) {
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap!!.asImageBitmap(),
                    contentDescription = title,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // For Audio List Items: Fast Instant Render with zero disk/IO overhead for 100+ songs!
            if (isListItem) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(placeholderGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                // For Hero Card (Single playing track): Full high-res album art decode
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .crossfade(false)
                        .size(128)
                        .build(),
                    contentDescription = title,
                    contentScale = contentScale,
                    onError = { hasError = true },
                    onSuccess = { hasError = false },
                    modifier = Modifier.fillMaxSize()
                )

                if (hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(placeholderGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
