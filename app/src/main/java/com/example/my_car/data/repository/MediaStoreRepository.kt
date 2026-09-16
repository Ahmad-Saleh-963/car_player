package com.example.my_car.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.my_car.data.model.MediaTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreRepository(private val context: Context) {

    suspend fun loadAudioTracks(): List<MediaTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<MediaTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "مسار صوتي"
                    val artist = cursor.getString(artistColumn) ?: "فنان مجهول"
                    val album = cursor.getString(albumColumn) ?: "ألبوم غير معروف"
                    val duration = cursor.getLong(durationColumn)
                    val filePath = cursor.getString(dataColumn) ?: ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val folderName = if (filePath.isNotEmpty()) {
                        File(filePath).parentFile?.name ?: "الموسيقى"
                    } else {
                        "الموسيقى"
                    }

                    tracks.add(
                        MediaTrack(
                            id = id,
                            uri = contentUri,
                            title = title,
                            artist = if (artist == "<unknown>") "فنان مجهول" else artist,
                            album = album,
                            durationMs = duration,
                            folderName = folderName,
                            filePath = filePath,
                            isVideo = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        tracks
    }

    suspend fun loadVideoTracks(): List<MediaTrack> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<MediaTrack>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA
        )
        val sortOrder = "${MediaStore.Video.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "مقطع فيديو"
                    val duration = cursor.getLong(durationColumn)
                    val filePath = cursor.getString(dataColumn) ?: ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val folderName = if (filePath.isNotEmpty()) {
                        File(filePath).parentFile?.name ?: "الفيديوهات"
                    } else {
                        "الفيديوهات"
                    }

                    videos.add(
                        MediaTrack(
                            id = id,
                            uri = contentUri,
                            title = title,
                            artist = "فيديو محلي",
                            album = "الاستوديو",
                            durationMs = duration,
                            folderName = folderName,
                            filePath = filePath,
                            isVideo = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        videos
    }
}
