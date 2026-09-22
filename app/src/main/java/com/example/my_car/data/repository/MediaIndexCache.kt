package com.example.my_car.data.repository

import com.example.my_car.data.model.MediaTrack
import androidx.compose.runtime.Immutable

@Immutable
data class AudioFolder(
    val name: String,
    val tracks: List<MediaTrack>
) {
    val trackCount: Int get() = tracks.size
}

@Immutable
data class ArtistGroup(
    val name: String,
    val tracks: List<MediaTrack>
) {
    val trackCount: Int get() = tracks.size
}

object MediaIndexCache {
    @Volatile
    var isIndexed: Boolean = false

    @Volatile
    var allAudioTracks: List<MediaTrack> = emptyList()

    @Volatile
    var audioFolders: List<AudioFolder> = emptyList()

    @Volatile
    var artistGroups: List<ArtistGroup> = emptyList()

    @Volatile
    var folderMap: Map<String, List<MediaTrack>> = emptyMap()

    @Volatile
    var artistMap: Map<String, List<MediaTrack>> = emptyMap()

    @Volatile
    var allVideoTracks: List<MediaTrack> = emptyList()

    @Volatile
    var allPhotoTracks: List<MediaTrack> = emptyList()

    @Volatile
    var photoFolders: List<AudioFolder> = emptyList()

    fun updateAudioIndex(tracks: List<MediaTrack>) {
        val groupedFoldersMap = tracks.groupBy { it.folderName }
        val groupedArtistsMap = tracks.groupBy { it.artist }

        val foldersList = groupedFoldersMap
            .map { (folderName, folderTracks) -> AudioFolder(folderName, folderTracks) }
            .sortedBy { it.name }

        val artistsList = groupedArtistsMap
            .map { (artistName, artistTracks) -> ArtistGroup(artistName, artistTracks) }
            .sortedBy { it.name }

        allAudioTracks = tracks
        folderMap = groupedFoldersMap
        artistMap = groupedArtistsMap
        audioFolders = foldersList
        artistGroups = artistsList
        isIndexed = true
    }

    fun updateVideoIndex(videos: List<MediaTrack>) {
        allVideoTracks = videos
    }

    fun updatePhotoIndex(photos: List<MediaTrack>) {
        allPhotoTracks = photos
        photoFolders = photos.groupBy { it.folderName }
            .map { (folderName, folderPhotos) -> AudioFolder(folderName, folderPhotos) }
            .sortedBy { it.name }
    }

    fun clear() {
        isIndexed = false
        allAudioTracks = emptyList()
        audioFolders = emptyList()
        artistGroups = emptyList()
        folderMap = emptyMap()
        artistMap = emptyMap()
        allVideoTracks = emptyList()
        allPhotoTracks = emptyList()
        photoFolders = emptyList()
    }
}
