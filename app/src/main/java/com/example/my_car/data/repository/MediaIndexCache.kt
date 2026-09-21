package com.example.my_car.data.repository

import com.example.my_car.data.model.MediaTrack

data class AudioFolder(
    val name: String,
    val tracks: List<MediaTrack>
) {
    val trackCount: Int get() = tracks.size
}

data class ArtistGroup(
    val name: String,
    val tracks: List<MediaTrack>
) {
    val trackCount: Int get() = tracks.size
}

object MediaIndexCache {
    @Volatile
    var isIndexed: Boolean = false

    var allAudioTracks: List<MediaTrack> = emptyList()
    var audioFolders: List<AudioFolder> = emptyList()
    var artistGroups: List<ArtistGroup> = emptyList()
    var allVideoTracks: List<MediaTrack> = emptyList()

    fun updateAudioIndex(tracks: List<MediaTrack>) {
        allAudioTracks = tracks

        // Pre-build immutable Folder index (Done once on IO Thread during scan)
        audioFolders = tracks.groupBy { it.folderName }
            .map { (folderName, folderTracks) -> AudioFolder(folderName, folderTracks) }
            .sortedBy { it.name }

        // Pre-build immutable Artist index (Done once on IO Thread during scan)
        artistGroups = tracks.groupBy { it.artist }
            .map { (artistName, artistTracks) -> ArtistGroup(artistName, artistTracks) }
            .sortedBy { it.name }

        isIndexed = true
    }

    fun updateVideoIndex(videos: List<MediaTrack>) {
        allVideoTracks = videos
    }

    fun clear() {
        isIndexed = false
        allAudioTracks = emptyList()
        audioFolders = emptyList()
        artistGroups = emptyList()
        allVideoTracks = emptyList()
    }
}
