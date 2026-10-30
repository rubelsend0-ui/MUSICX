package com.soniqxpro.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniqxpro.database.PlaylistEntity
import com.soniqxpro.database.PlaylistWithCount
import com.soniqxpro.database.SongEntity
import com.soniqxpro.playback.PlayerConnection
import com.soniqxpro.repository.FavoriteRepository
import com.soniqxpro.repository.LibraryRepository
import com.soniqxpro.repository.PlaylistRepository
import com.soniqxpro.repository.RecentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(repo: FavoriteRepository) : ViewModel() {
    val songs: StateFlow<List<SongEntity>?> = repo.observeSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@HiltViewModel
class RecentViewModel @Inject constructor(private val repo: RecentRepository) : ViewModel() {
    val songs: StateFlow<List<SongEntity>?> = repo.observeSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun clear() {
        viewModelScope.launch {
            repo.clear().fold(
                onSuccess = { _messages.tryEmit("Recently played cleared") },
                onFailure = { _messages.tryEmit(it.message ?: "Could not clear history") }
            )
        }
    }
}

@HiltViewModel
class PlaylistsViewModel @Inject constructor(private val repo: PlaylistRepository) : ViewModel() {
    val playlists: StateFlow<List<PlaylistWithCount>?> = repo.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun create(name: String) {
        viewModelScope.launch {
            repo.create(name).onFailure { _messages.tryEmit(it.message ?: "Could not create playlist") }
        }
    }

    fun rename(id: Long, name: String) {
        viewModelScope.launch {
            repo.rename(id, name).onFailure { _messages.tryEmit(it.message ?: "Could not rename playlist") }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repo.delete(id).fold(
                onSuccess = { _messages.tryEmit("Playlist deleted") },
                onFailure = { _messages.tryEmit(it.message ?: "Could not delete playlist") }
            )
        }
    }
}

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: PlaylistRepository,
    library: LibraryRepository,
    private val player: PlayerConnection
) : ViewModel() {
    private val playlistId: Long = checkNotNull(savedStateHandle.get<Long>("id"))

    val playlist: StateFlow<PlaylistEntity?> = repo.observePlaylist(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val songs: StateFlow<List<SongEntity>> = repo.observeSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val librarySongs: StateFlow<List<SongEntity>> = library.songs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun removeSong(song: SongEntity) {
        viewModelScope.launch {
            repo.removeSong(playlistId, song.id).onFailure { _messages.tryEmit(it.message ?: "Could not remove song") }
        }
    }

    fun move(from: Int, to: Int) {
        viewModelScope.launch {
            repo.move(playlistId, from, to).onFailure { _messages.tryEmit(it.message ?: "Could not reorder") }
        }
    }

    fun addSongs(ids: List<String>) {
        viewModelScope.launch {
            repo.addSongs(playlistId, ids).fold(
                onSuccess = { _messages.tryEmit("Added $it song(s)") },
                onFailure = { _messages.tryEmit(it.message ?: "Could not add songs") }
            )
        }
    }

    fun playAll(shuffled: Boolean) {
        val list = songs.value
        if (list.isEmpty()) return
        player.playQueue(if (shuffled) list.shuffled() else list, 0)
    }
}
