package com.soniqxpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniqxpro.database.PlaylistWithCount
import com.soniqxpro.database.SongEntity
import com.soniqxpro.playback.PlayerConnection
import com.soniqxpro.repository.FavoriteRepository
import com.soniqxpro.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Shared per-row actions (play, favorite, add to playlist) used by every song list. */
@HiltViewModel
class SongActionsViewModel @Inject constructor(
    private val player: PlayerConnection,
    private val favoriteRepo: FavoriteRepository,
    private val playlistRepo: PlaylistRepository
) : ViewModel() {

    val favoriteIds: StateFlow<Set<String>> = favoriteRepo.observeIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val playlists: StateFlow<List<PlaylistWithCount>> = playlistRepo.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentMediaId: StateFlow<String?> = player.state
        .map { it.mediaId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun play(songs: List<SongEntity>, index: Int) {
        player.playQueue(songs, index)
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            favoriteRepo.toggle(song.id).fold(
                onSuccess = { _messages.tryEmit(if (it) "Added to favorites" else "Removed from favorites") },
                onFailure = { _messages.tryEmit(it.message ?: "Could not update favorites") }
            )
        }
    }

    fun addToPlaylist(playlistId: Long, song: SongEntity) {
        viewModelScope.launch {
            playlistRepo.addSongs(playlistId, listOf(song.id)).fold(
                onSuccess = { _messages.tryEmit(if (it > 0) "Added to playlist" else "Already in this playlist") },
                onFailure = { _messages.tryEmit(it.message ?: "Could not add to playlist") }
            )
        }
    }

    fun createPlaylistAndAdd(name: String, song: SongEntity) {
        viewModelScope.launch {
            val id = playlistRepo.create(name).getOrElse {
                _messages.tryEmit(it.message ?: "Could not create playlist")
                return@launch
            }
            addToPlaylist(id, song)
        }
    }
}
