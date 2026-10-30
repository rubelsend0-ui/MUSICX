package com.musicx.player.data.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("duration") val duration: Double = 0.0,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("bitrate") val bitrate: Int? = null,
    @SerializedName("file_format") val fileFormat: String? = null,
    @SerializedName("file_size") val fileSize: Long = 0,
    @SerializedName("file_path") val filePath: String? = null,
    @SerializedName("has_artwork") val hasArtwork: Int = 0
) {
    val displayArtist: String
        get() = if (artist.isNullOrBlank() || artist.equals("Unknown Artist", ignoreCase = true)) "Unknown Artist" else artist

    val displayAlbum: String
        get() = if (album.isNullOrBlank() || album.equals("Unknown Album", ignoreCase = true)) "Single" else album

    val displayTitle: String
        get() = if (title.isBlank()) "Untitled Track" else title

    val durationMs: Long
        get() = (duration * 1000).toLong()

    val formattedDuration: String
        get() {
            val totalSeconds = duration.toInt()
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

data class LibraryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("pagination") val pagination: PaginationInfo?,
    @SerializedName("songs") val songs: List<Song> = emptyList()
)

data class PaginationInfo(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class SearchResponse(
    @SerializedName("status") val status: String,
    @SerializedName("query") val query: String?,
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<Song> = emptyList()
)

data class ArtistItem(
    @SerializedName("artist") val artist: String,
    @SerializedName("track_count") val trackCount: Int
)

data class ArtistsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("artists") val artists: List<ArtistItem> = emptyList()
)

data class AlbumItem(
    @SerializedName("album") val album: String,
    @SerializedName("artist") val artist: String?,
    @SerializedName("track_count") val trackCount: Int
)

data class AlbumsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("albums") val albums: List<AlbumItem> = emptyList()
)

data class GenreItem(
    @SerializedName("genre") val genre: String,
    @SerializedName("track_count") val trackCount: Int
)

data class GenresResponse(
    @SerializedName("status") val status: String,
    @SerializedName("genres") val genres: List<GenreItem> = emptyList()
)

data class StatsData(
    @SerializedName("total_songs") val totalSongs: Int,
    @SerializedName("total_duration_seconds") val totalDurationSeconds: Double,
    @SerializedName("total_size_bytes") val totalSizeBytes: Long
)

data class StatsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("stats") val stats: StatsData?
)

data class HealthResponse(
    @SerializedName("status") val status: String,
    @SerializedName("uptimeSeconds") val uptimeSeconds: Long
)

data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String> = emptyList()
)
