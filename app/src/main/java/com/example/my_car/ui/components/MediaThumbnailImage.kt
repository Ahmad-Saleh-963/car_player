package com.example.my_car.ui.components

import android.content.ContentUris
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
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

@Composable
fun MediaThumbnailImage(
    uri: Uri,
    title: String,
    isVideo: Boolean,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var thumbnailBitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var hasError by remember(uri) { mutableStateOf(false) }

    // Asynchronously load real thumbnail or video frame
    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                if (isVideo) {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(context, uri)
                    val frame = mmr.frameAtTime
                    mmr.release()
                    thumbnailBitmap = frame
                }
            } catch (e: Exception) {
                hasError = true
            }
        }
    }

    val placeholderGradient = remember(isVideo) {
        if (isVideo) {
            Brush.linearGradient(colors = listOf(Color(0xFF881337), Color(0xFFE11D48)))
        } else {
            Brush.linearGradient(colors = listOf(Color(0xFF0284C7), Color(0xFF0F172A)))
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        if (thumbnailBitmap != null) {
            Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = title,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = contentScale,
                onError = { hasError = true },
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
                        imageVector = if (isVideo) Icons.Default.Movie else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
