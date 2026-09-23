package com.example.my_car.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.my_car.data.model.MediaTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaStoreRepository(private val context: Context) {

    suspend fun loadAudioTracks(forceRefresh: Boolean = false): List<MediaTrack> = withContext(Dispatchers.IO) {
        if (!forceRefresh && MediaIndexCache.isIndexed && MediaIndexCache.allAudioTracks.isNotEmpty()) {
            return@withContext MediaIndexCache.allAudioTracks
        }

        val tracks = mutableListOf<MediaTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%'"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = if (idColumn != -1) cursor.getLong(idColumn) else 0L
                    val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "مسار صوتي" else "مسار صوتي"
                    val artist = if (artistColumn != -1) cursor.getString(artistColumn) ?: "فنان مجهول" else "فنان مجهول"
                    val album = if (albumColumn != -1) cursor.getString(albumColumn) ?: "ألبوم غير معروف" else "ألبوم غير معروف"
                    val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                    val filePath = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""

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
                            isVideo = false,
                            isImage = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MediaIndexCache.updateAudioIndex(tracks)
        tracks
    }

    suspend fun loadVideoTracks(forceRefresh: Boolean = false): List<MediaTrack> = withContext(Dispatchers.IO) {
        if (!forceRefresh && MediaIndexCache.allVideoTracks.isNotEmpty()) {
            return@withContext MediaIndexCache.allVideoTracks
        }

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
                val idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = if (idColumn != -1) cursor.getLong(idColumn) else 0L
                    val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "مقطع فيديو" else "مقطع فيديو"
                    val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                    val filePath = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""

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
                            isVideo = true,
                            isImage = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MediaIndexCache.updateVideoIndex(videos)
        videos
    }

    suspend fun loadPhotoTracks(forceRefresh: Boolean = false): List<MediaTrack> = withContext(Dispatchers.IO) {
        if (!forceRefresh && MediaIndexCache.allPhotoTracks.isNotEmpty()) {
            return@withContext MediaIndexCache.allPhotoTracks
        }

        val photos = mutableListOf<MediaTrack>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATA
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val titleColumn = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                val dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

                while (cursor.moveToNext()) {
                    val id = if (idColumn != -1) cursor.getLong(idColumn) else 0L
                    val title = if (titleColumn != -1) cursor.getString(titleColumn) ?: "صورة" else "صورة"
                    val filePath = if (dataColumn != -1) cursor.getString(dataColumn) ?: "" else ""

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val folderName = if (filePath.isNotEmpty()) {
                        File(filePath).parentFile?.name ?: "الصور"
                    } else {
                        "الصور"
                    }

                    photos.add(
                        MediaTrack(
                            id = id,
                            uri = contentUri,
                            title = title,
                            artist = "صورة محليّة",
                            album = "الاستوديو",
                            durationMs = 0L,
                            folderName = folderName,
                            filePath = filePath,
                            isVideo = false,
                            isImage = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        MediaIndexCache.updatePhotoIndex(photos)
        photos
    }
}
