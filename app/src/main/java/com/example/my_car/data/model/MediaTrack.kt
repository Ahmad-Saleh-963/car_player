package com.example.my_car.data.model

import android.net.Uri
import java.io.File
import java.util.Locale

data class MediaTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val folderName: String,
    val filePath: String,
    val isVideo: Boolean = false,
    val isFavorite: Boolean = false
) {
    fun formatDuration(): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatSize(): String {
        val file = File(filePath)
        if (!file.exists()) return ""
        val bytes = file.length()
        val mb = bytes / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }
}
