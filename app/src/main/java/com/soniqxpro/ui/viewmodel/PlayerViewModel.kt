package com.soniqxpro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soniqxpro.playback.PlayerConnection
import com.soniqxpro.playback.PlayerState
import com.soniqxpro.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: PlayerConnection,
    private val favoriteRepo: FavoriteRepository
) : ViewModel() {

    val state: StateFlow<PlayerState> = player.state

    val hasMedia: StateFlow<Boolean> = player.state.map { it.hasMedia }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val error: StateFlow<String?> = player.state.map { it.error }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isFavorite: StateFlow<Boolean> = combine(
        player.state.map { it.mediaId }.distinctUntilChanged(),
        favoriteRepo.observeIds()
    ) { id, ids -> id != null && id in ids }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun togglePlay() = player.togglePlayPause()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seekTo(ms: Long) = player.seekTo(ms)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeat() = player.cycleRepeat()
    fun setSpeed(speed: Float) = player.setSpeed(speed)
    fun skipTo(index: Int) = player.skipTo(index)
    fun removeFromQueue(index: Int) = player.removeFromQueue(index)
    fun moveInQueue(from: Int, to: Int) = player.moveInQueue(from, to)
    fun clearError() = player.clearError()

    fun toggleFavorite() {
        val id = player.state.value.mediaId ?: return
        viewModelScope.launch { favoriteRepo.toggle(id) }
    }
}
