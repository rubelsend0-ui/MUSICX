package com.musicx.player.data.repository

import com.musicx.player.data.local.LocalLibraryStore
import com.musicx.player.data.model.*
import com.musicx.player.data.remote.NetworkClientProvider
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val network: NetworkClientProvider,
    private val local: LocalLibraryStore
) {
    val favorites: Flow<Set<String>> = local.favoritesFlow
    val recents: Flow<List<Song>> = local.recentsFlow
    val playlists: Flow<List<Playlist>> = local.playlistsFlow

    suspend fun getLibrary(page: Int, limit: Int = 50) = network.getService().getLibrary(page, limit)
    suspend fun search(query: String) = network.getService().search(query)
    suspend fun getArtists() = network.getService().getArtists()
    suspend fun getAlbums() = network.getService().getAlbums()
    suspend fun getGenres() = network.getService().getGenres()
    suspend fun getRandom(limit: Int = 10) = network.getService().getRandom(limit)
    suspend fun getStats() = network.getService().getStats()
    suspend fun getHealth() = network.getService().getHealth()

    suspend fun toggleFavorite(songId: String) = local.toggleFavorite(songId)
    suspend fun recordRecent(song: Song) = local.addRecent(song)
    suspend fun createPlaylist(name: String) = local.createPlaylist(name)
    suspend fun addSongToPlaylist(plId: String, songId: String) = local.addSongToPlaylist(plId, songId)
}
